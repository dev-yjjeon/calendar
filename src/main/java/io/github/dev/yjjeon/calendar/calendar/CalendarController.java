package io.github.dev.yjjeon.calendar.calendar;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CalendarController {

	@GetMapping({"/", "/calendar"})
	public String calendarPage() {
		return "calendar";
	}

}
