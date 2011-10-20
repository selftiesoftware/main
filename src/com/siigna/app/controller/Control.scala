/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */
package com.siigna.app.controller

import collection.mutable.{Queue, Stack}

import com.siigna.app.controller.command._
import com.siigna.module.Module
import com.siigna.util.event.{Event, ModuleEvent}
import com.siigna.util.logging.Log
import com.siigna.app.Siigna

/**
 * The Control controls the core of the software. Basically that includes
 * dealing with the event-flow to the modules.
 */
object Control extends Thread("Siigna Controller") {

  /**
   * The last 10 events
   */
  private var events : List[Event] = List()
  
  /**
   * Queued events waiting to be handled.
   */
  private val eventQueue = new Queue[Event]()

  /**
   * Queued commands waiting to be handled.
   */
  private val commandQueue = new Queue[Command]()

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
   * Shortcut for propagating events.
   */
  def apply(event : Event) { dispatchEvent(event) }

  /**
   * Shortcut for propagating messages.
   */
  def apply(command : Command) { dispatchCommand(command) }

  /**
   * Add an event to the event queue.
   */
  def dispatchEvent(event : Event) {
    eventQueue enqueue event

    // State that the incoming event cannot possibly be recycled from a module ending.
    isForwardedEvent = false
  }

  /**
   * Add a command to the command queue.
   */
  def dispatchCommand(command : Command) { commandQueue enqueue command }
  
  /**
   * Return the last 10 parsed events currently stored in the controller.
   */
  def getEvents = events

  /**
   * Initializes a module.
   *
   * @return A Boolean value indicating whether the module was successfully initialized.
   */
  private def initModule(module : Module) : Boolean = try {
    // Set the state to start!
    module.state = 'Start

    // Get the interface
    val interface = module.interface

    // Give the paint-function to the interface
    interface.setPaint(module.paint)

    // Give Siigna the new interface in hand
    Siigna.interface = interface
    true
  } catch {
    // Log the failure
    case e => Log.warning("Controller: Failed to initialize module " + module + ". The module will still run, but the painting is probably messed up.")
    false
  }
  
  /**
   * The running part of the controller.
   *
   * It consists of a loop that evaluates true as long as <code>isRunning</code> is true. When set to false the thread dies.
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
   * Lastly we check for pending commands and takes care of them.
   */
  override def run() { try {

    // Initialize the Default module and Menu module.
    com.siigna.Preload('Default)
    com.siigna.Preload('Menu)
    com.siigna.ForwardTo('Default)

    // Run the loop
    while(true) {
      // Check there's modules to execute code from
      if (modules.isEmpty) {
        // If there's no commands in the pipe (e. g. Preload or ForwardTo), then shut the controller down...
        if (commandQueue.isEmpty) {
          Log.error("Controller: No modules in the controller. Shutting down...")
          interrupt()
        }
      } else {
        // Checks that there's an even waiting to be processed
        if (!eventQueue.isEmpty && !modules.isEmpty) {

          // Check whether the event is forwarded, and thus needs to be discarded
          if (isForwardedEvent) {
            // Kaboom!
            eventQueue.dequeue()
            // .......

            // Stop the inhumane destruction of events
            isForwardedEvent = false

          // Otherwise we're good to go!
          } else {
            // Retrieve event
            val event = eventQueue dequeue()

            // Retrieve module
            val module : Module = modules.top

            // Parse the events
            events = module.eventParser.parse(event :: events)

            // Give the module a chance to change state
            module.eventHandler.stateMap(module.state -> events.head.symbol) match {
              case Some(s : Symbol) => if (module.state != s) {
                module.state = s
                Log.info(this + " succesfully changed the state of the active module to "+s)
              }
              case None => Log.debug(this + " tried to change state with event "+events.head+", but no route was found.")
            }

            // React on the event parsed and execute the function associated with the state;
            // These lines are in a try-catch loop in case anything goes wrong in a module.
            // Since modules are prone to error we need to make sure they don't break the entire program.}

            val result : Any = try {
              // Retrieve the function from the map and apply them if they exist
              module.eventHandler.stateMachine.get(module.state) match {
                case Some(f) => f(events)
                case None =>
              }
            } catch {
              case e => Log.error("Error in retrieving state machine from module " + module + ".", e)
            }

            // If the module is ending then send a <code>ModuleEvent</code>s back into the
            // event-queue for other modules to respond on
            if (module.state == 'End) result match {
              // Put a module event back in the event queue
              case moduleEvent : ModuleEvent => eventQueue enqueue moduleEvent
              case unknown => Log.debug("Control: Received object "+unknown+" from the active modules state machine, " +
                "but not reacting since it's not a Module Event.")
            }

            // Check for current state. If modules is ending remove the most recent module and store it's events
            // back in the queue so the next loop returns true (and the "parent" gets a chance to act).
            if (module.state == 'End) {
              // Remove the ending module from the module stack.
              modules.pop()

              // Store the head of the event-list in the event-queue.
              eventQueue.enqueue(events.head)

              // Set the most dangerous isForwardedEvent variable!
              isForwardedEvent = true

              // Store the tail of the events in the next module in the module stack.
              events = events.tail

              // Set the new interface
              if (!modules.isEmpty) {
                initModule(modules.top)

                Log.success("Control: Successfully ended module. Current module: "+modules.top)
              }
            }
          }
        }
      }

      // Check for pending commands.
      if (!commandQueue.isEmpty) {
        // Retrieve command
        val command = commandQueue dequeue()

        // Match on the command
        command match {
          // Forward to another module
          case ForwardTo(symbol, continue) => {
            // Try to find the module
            val loadedModule = moduleBank.load(symbol)

            // Save the modules if defined
            if (loadedModule.isDefined) {
              // Put the new module into the stack.
              modules.push(loadedModule.get)

              // Put the latest event back in the event-queue if it's specified in the ForwardTo command.
              if (continue && !events.isEmpty) {
                eventQueue.+=:(events.head)
	              events = events.tail
              }

              // Initialize module
              if (initModule(modules.top)) {
                // Log the success
                Log.success("Controller: Succesfully forwarded to "+symbol+".")
              }
            } else {
              Log.warning("Control: Failed to forward to module "+symbol+". Could not find it")
            }
          }
          // Goto another state
          case Goto(state, continue) => {
            if (modules.isEmpty) {
              Log.warning("Controller could not goto another stat - no module in the stack.")
            } else {
              // Set the new state
	            modules.top.state = state
	
	            // If continue is set, prepend the newest event to the event-queue
	            // and remove the newest event form the event-list
	            if (continue) {
	              eventQueue.+=:(events.head)
	              events = events.tail
	              Log.info("Controller successfully changed state to "+state+" and continued execution of the new state.")
	            } else {
	              Log.info("Controller successfully changed state to "+state+".")
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
      
      // Hint the processor that the thread can spare some resources
      Thread.`yield`() // In scala "yield" is reserved, so we use "`" to escape
      
      // Sleep for a while to avoid using too many resources
      Thread.sleep(sleepTime)
      
      // Terminate the thread if it's been interrupted
      if (Thread.currentThread().isInterrupted)
        throw new InterruptedException()
    }
  } catch {
    case e : InterruptedException => Log.info("Control has been terminated.")
    case e => Log.error("Control was terminated with unexpected error.", e)
  }}

}