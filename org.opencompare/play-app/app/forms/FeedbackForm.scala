package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the submission of a feedback.
 */
object FeedbackForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "email" -> email,
      "subject" -> nonEmptyText,
      "content" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param email The email of the user.
   * @param password The password of the user.
   * @param rememberMe Indicates if the user should stay logged in on the next visit.
   */
  case class Data(
    email: String,
    subject: String,
    content: String)
}
