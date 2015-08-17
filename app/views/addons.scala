package views


package object addons {

  import java.util.Date
  import org.joda.time.DateTime;
  import org.joda.time.Period;

  class PimpedDate(col: Date) {

    def since() = {
      def addS(b: Int) = if (b == 1) "" else "s"

      val now: DateTime = new DateTime();
      val period: Period = new Period(new DateTime(col), now);
      var r: String = "";

      if(period.getYears() < 0) {
          r = " in " + -period.getYears() + " year" + addS(period.getYears()); 
      } else if (period.getWeeks() < 0) {
        r = " in " + -period.getWeeks() + " week" + addS(period.getWeeks()); 
      } else if (period.getMonths() < 0) {
        r = " in " + -period.getMonths() + " month" + addS(period.getMonths()); 
      } else if (period.getDays() < 0) {
        r = " in " + -period.getDays() + " day" + addS(period.getDays()); 
      } else if (period.getHours() < 0) {
        r = " in " + -period.getHours() + " hour" + addS(period.getHours()); 
      } else if (period.getMinutes() < 0) {
        r = " in " + -period.getMinutes() + " minute" + addS(period.getMinutes()); 
      } else if(period.getSeconds() < 0) {
        r = " in " + -period.getSeconds() + " second" + addS(period.getSeconds()); 
      } else if (period.getYears() > 0) {
        r = period.getYears() + " year" + addS(period.getYears()) + " ago";
      } else if (period.getWeeks() > 0) {
        r = period.getWeeks() + " week" + addS(period.getWeeks()) + " ago";
      } else if (period.getMonths() > 0) {
        r = period.getMonths() + " month" + addS(period.getMonths()) + " ago";
      } else if (period.getDays() > 0) {
        r = period.getDays() + " day" + addS(period.getDays()) + " ago";
      } else if (period.getHours() > 0) {
        r = period.getHours() + " hour" + addS(period.getHours()) + " ago";
      } else if (period.getMinutes() > 0) {
        r = period.getMinutes() + " minute" + addS(period.getMinutes()) + " ago";
      } else {
        r = period.getSeconds() + " second" + addS(period.getSeconds()) + " ago";
      }

      r
    }
  }

  implicit def pimpDate(col: Date) = new PimpedDate(col)
}

