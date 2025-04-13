package com.hmshk.interceptor;

import com.hmshk.service.BookingService;
import com.hmshk.service.FacilityBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class RequestCountInterceptor implements HandlerInterceptor {

    private final BookingService bookingService;
    private final FacilityBookingService facilityBookingService;

    @Autowired
    public RequestCountInterceptor(BookingService bookingService, FacilityBookingService facilityBookingService) {
        this.bookingService = bookingService;
        this.facilityBookingService = facilityBookingService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        HttpSession session = request.getSession();
        if (session.getAttribute("staff") != null) {
            // Count pending booking requests
            int bookingRequestCount = bookingService.countPendingRequests();
            session.setAttribute("bookingRequestCount", bookingRequestCount);
            
            // Count pending facility booking requests
            int facilityRequestCount = facilityBookingService.countPendingRequests();
            session.setAttribute("facilityRequestCount", facilityRequestCount);
            
            // Total pending requests
            int totalPendingRequests = bookingRequestCount + facilityRequestCount;
            session.setAttribute("totalPendingRequests", totalPendingRequests);
        }
    }
}
