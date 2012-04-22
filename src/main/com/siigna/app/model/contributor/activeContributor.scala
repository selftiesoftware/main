package com.siigna.app.model.contributor


object activeContributor {

  var contributorName: Option[String] = None
  var contributorId: Option[Int] = None

  def getContributorNameFromHomepage() = {
    contributorName = com.siigna.app.controller.AppletParameters.getParametersString("contributorName")
    (contributorName)
  }

}
