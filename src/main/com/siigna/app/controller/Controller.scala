/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */
package com.siigna.app.controller

import collection.mutable.Stack

import com.siigna.app.controller.command._
import com.siigna.app.view.event.{Event, ModuleEvent}
import com.siigna.module.Module
import com.siigna.util.logging.Log
import com.siigna.app.Siigna
import com.siigna.app.model.action.Action
import actors.Actor
import actors.remote.RemoteActor._
import actors.remote.{RemoteActor, Node}
import com.siigna.app.model.Model
import remote._
import com.siigna.app.controller.AppletParameters._

/**
 * The Controller controls the core of the software. Basically that includes
 * dealing with the event-flow to the modules.
 *
 * TODO: Implement this as one single actor.
 */
object Controller extends Actor {

  // Set remote class loader
  RemoteActor.classLoader = getClass.getClassLoader
  
  var client : Option[Client] = None

  /**
   * The last 10 events
   */
  private var events : List[Event] = List()

  /**
   * The stack of active Modules, ranging from the "oldest" in the bottom of the stack to the newest and active module
   * in the top. LIFO.
   */
  private val modules = new Stack[Module]()

  /**
   * The Module Bank used to fetch modules from external sources.
   */
  private val moduleBank = new ModuleBank()

  /**
   * The interval with which to check for incoming events and actions. Defaults to 5.
   */
  var sleepTime = 5

  /**
   * A boolean value to indicate whether an event has been used to forward to a module and sent back again
   * when the module ended. If so it should not be sent back since it could create an endless loop.
   * <br />
   * Imagine a module forwarding an event, ending and then putting the last event back
   * in the event-queue, which again are given to the same module. This could result in an endless loop.
   * <br />
   * The variable resets every time a new event gets into the queue.
   * <br />
   * P.s. Yes I am aware that no scaladoc is generated from private members.
   */
  private var isForwardedEvent = false

  /**
   * The running part of the controller.
   *
   * It consists of a loop that exists until 'exit is given, or the system is exiting (naturally).
   *
   * In the loop we first examine whether there is pending events. If so we:
   * <ol>
   *   <li>Set the state of the active module.</li>
   *   <li>React on the given event by executing the state given by the state machine.</li>
   *   <li>Close a possibly ending module and ask the loop to repeat so the "parent" can answer.</li>
   *   <li>If the ending module returns a <code>ModuleEvent</code> then we put it back into the event queue, so other
   *       modules can react on it.</li>
   * </ol>
   *
   * The actor also handles commands and the 'exit symbol.
   */
  def act() {
    // Define the sink
    val sink = select(Node("siigna.com", 20004), 'siigna)

    // Register the client
    // Remember that commands are sent to the controller immediately after creation
    // TODO: Insert drawing-id here
    println("Registering")
    Register(AppletParameters.contributorName, AppletParameters.drawingId)

    // Loop and react on incoming messages
    loop {
      react {

        // Forward incoming actions to the server
        case action : Action => {
          Model execute action
          println("Controller recieved action: "+action)
        }

        // Handle remote commands
        case command : RemoteCommand => {
          Log.debug("Controller: Received remote command: " + command)
          println("Kommando modtaget i controller: " + command)

          // Match the received command
          command match {
            // Catch successes - we know these are from the server
            case success : Success => {
              // Examine what was successful
              success.command match {

                // Successful registration of the client
                case r : Register => {
                  client = Some(r.client)
                  val id = client.get.id
                  Log.info("Controller registered client with id " + id)
                  //Gemmer klienten, der identificerer appletten overfor serveren, i AppletParameters
                  AppletParameters.setClient(client)
                  //Hvis der er kommet en aktiv tegning fra hjemmesiden hentes den, ellers laves der en ny:
                  if (AppletParameters.drawingId.isDefined) {
                    var drawingId = com.siigna.app.controller.AppletParameters.drawingId
                    if (!drawingId.isDefined) drawingId = Some(1)
                    sink ! GetDrawing(drawingId.get, client.get)
                  } else {
                    GetNewDrawingId(getClient)
                  }
                  //get a specified number of new shapeIds from the server, ready to use for new shapes
                  GetNewShapeIds(2,AppletParameters.getClient)
                }
                case _ =>
              }
            }            
            // Forward everything else to the server. If it is not a Success type we can be
            // sure the remote command are meant to be forwarded to the server
            case _ => sink ! command
          }
        }

        case command : NewDrawingId => {
          AppletParameters.drawingId = command.retrieveNewDrawingId
        }
        case command : NewShapeId => {
          AppletParameters.receiveNewShapeId(command.retrieveNewShapeId)
        }
        case command : NewShapeIds => {
          AppletParameters.receiveNewShapeIds(command.retrieveNewShapeIds)
        }


        // Handle ordinary commands (from the modules), and RemoteMessages:
        case command : Command => {
          Log.debug("Controller: Received command: " + command)

          // Match on the command
          command match {
            // Forward to another module
            case ForwardTo(symbol, continue) => {
              // Try to find the module
              val loadedModule = moduleBank.load(symbol)

              // Save the modules if defined
              if (loadedModule.isDefined) {
                // Tell the old module it's no longer active
                if (modules.size > 0)
                  modules.head.isActive = false

                // Put the new module into the stack.
                modules.push(loadedModule.get)

                // Put the latest event back in the event-queue if it's specified in the ForwardTo command.
                if (continue && !events.isEmpty) {
                  this ! events.head
                  events = events.tail
                }

                // Initialize module
                val success = startModule(modules.top)

                // Log the success
                if (success) {
                  Log.success("Controller: Succesfully forwarded to "+symbol+".")
                }
              } else {
                Log.warning("Controller: Failed to forward to module "+symbol+". Could not find it")
              }
            }
            // Goto another state
            case Goto(state, continue) => {
              if (modules.isEmpty) {
                Log.warning("[Controller]: Could not change state - no module in the stack.")
              } else {
                // Set the new state
                modules.top.state = state

                // If continue is set, prepend the newest event to the event-queue
                // and remove the newest event form the event-list
                if (continue) {
                  this ! events.head
                  events = events.tail

                  Log.info("Controller successfully changed state of " + modules.top + " to "+state+" and continued execution.")
                } else {
                  Log.info("Controller successfully changed state of " + modules.top + " to "+state+".")
                }
              }
            }

            // Preload a module
            case Preload(name, classPath, filePath) => {
              moduleBank.preload(name, classPath, filePath)
            }
            // Match unknown commands
            case unknown => Log.warning("Controller received unknown command: "+unknown)
          }
        }
        case event : Event => {
          /*// Check whether the event is forwarded, and thus needs to be discarded
          if (isForwardedEvent) {
            // If there is a forwarded event and the head of the queue is a ModuleEvent
            // the ModuleEvent has been placed in front of the forwarded Event
            // - then enqueue the event again and destroy the next event
            if (event.isInstanceOf[ModuleEvent] && mailboxSize > 1) {
              this ! event

            // Otherwise the event is forwarded and we can simply ignore it and set
            // isForwardedEvent to false
            } else {
              // Stop the inhumane destruction of events
              isForwardedEvent = false
            }
          // Otherwise no forwarded events exist and we're good to go!
          } else*/
          if (modules.size > 0) {

            // Retrieve module
            val module : Module = modules.top

            // Examine if the module has not yet been imported
            // Parse the events
            events = module.eventParser.parse(event :: events)

            // Give the module a chance to change state
            try {
              module.eventHandler.stateMap(module.state -> events.head.symbol) match {
                case Some(s : Symbol) => if (module.state != s) {
                  module.state = s
                  Log.info("Controller: Succesfully changed the state of the " + module + " to "+s)
                }
                case None => Log.debug("Controller: Tried to change state with event "+events.head+", but no route was found.")
              }
            } catch {
              case e => Log.error("Controller: Unexpected error in processing state map: ", e)
            }

            // React on the event parsed and execute the function associated with the state;
            // These lines are in a try-catch loop in case anything goes wrong in a module.
            // Since modules are prone to error we need to make sure they don't break the entire program.
            val result : Any = try {
              // Retrieve the function from the map and apply them if they exist
              module.eventHandler.stateMachine.get(module.state) match {
                case Some(f) => f(events)
                case None =>
              }
            } catch {
              case e => Log.error("Error in retrieving state machine from module " + module + ".", e)
            }

            // If the module is ending then stop the module and match on the resulting event
            // If it was a ModuleEvent then send it back into the event-queue for other modules
            // to respond on.
            if (module.state == 'End) {

              val continue = result match {
                // Put a module event back in the event queue
                case moduleEvent : ModuleEvent => {
                  this ! moduleEvent
                  false
                }
                case unknown => {
                  Log.debug("Controller: Received object "+unknown+" from the ending module " + module +
                    ", but not reacting since it is not a Module Event.")
                  true
                }
              }

              // Stop the module
              stopModule(module, continue)
            }
          } else {
            Log.warning("Controller: No modules in the controller. Trying to forward to Default.")
            ForwardTo('Default)
          }
        }
        // Exit
        case 'exit => Log.info("Controller is shutting down"); exit()
        // Unknown
        case e => Log.warning("Controller: Received unknown input: " + e)
      }
    }
  }

  /**
   * Shortcut for propagating events.
   */
  def apply(event : Event) { this ! event; isForwardedEvent = false }

  /**
   * Shortcut for propagating messages.
   */
  def apply(command : Command) { this ! command }
  
  /**
   * Return the last 10 parsed events currently stored in the controller.
   */
  def getEvents = events

  /**
   * Initializes a module by setting the starting state to 'Start and chaining the modules
   * interface to the other interfaces (among other things this is the chain for painting -
   * see the <code>Interface</code> trait for more info.
   *
   * @param module  The module to initialize.
   * @param state  The state to start the module in. Defaults to 'Start
   * @return A Boolean value indicating whether the module was successfully initialized.
   */
  private def startModule(module : Module, state : Symbol = 'Start) : Boolean = try {
    // Set the state to start!
    module.state = state

    // Chain the interface higher up in the hierarchy with the new interface
    // or set the active interface in Siigna if the interface hasn't been defined
    if (modules.size > 1) {
      modules(modules.size - 1).interface.chain(module.interface)
    } else if (Siigna.getInterface.isEmpty) {
      Siigna.setInterface(module.interface)
    }

    // Give the paint-function to the interface
    module.interface.setPaint(module.paint)

    // Unchain the module if it happens to be chained
    if (module.interface.isChained)
      module.interface.unchain()

    // Tell the module that it's active!
    module.isActive = true
    
    // Return success!
    Log.success("Controller: Successfully initialized module: " + module)
    true
  } catch {
    // Log the failure
    case e => Log.warning("Controller: Failed to initialize the interface of " + module + ". The module will (probably) still run, but without a graphical output.")
    false
  }

  /**
   * Stops a module and enqueues the latest event for use in the module that takes over.
   * In other words we remove the most recent module and store it's events back in the queue so the next
   * control-loop if initiated and so the "parent" gets a chance to act.
   */
  private def stopModule(module : Module, continue : Boolean) {
    if (modules.size > 1) {
      // Tell the module that it's no longer active
      module.isActive = false

      // Remove the ending module from the module stack.
      modules.pop()

      // Store the head of the event-list in the event-queue.
      if (continue) {
        this ! events.head

        // Set the most dangerous isForwardedEvent variable!
        isForwardedEvent = true

        // Store the tail of the events in the next module in the module stack.
        events = events.tail
      }

      // Initialize the module
      if (startModule(modules.top, modules.top.state))
        Log.success("Controller: Successfully ended " + module + ". Current module: "+modules.top)
      else
        Log.error("Controller: Sucessfully ended " + module + ", but unable to initialize the parent: " + modules.top)
    } else Log.error("Controller: Unable to stop module " + modules.head + " - it's the only module left.")
  }

}