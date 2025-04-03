// /src/main/resources/static/js/calendar.js
document.addEventListener('DOMContentLoaded', function() {
    // Get the calendar element
    const calendarEl = document.getElementById('calendar');
    
    // Only initialize if the element exists
    if (calendarEl) {
        // Get calendar events data from model (passed from controller)
        // This ensures data is fresh every time the page loads
        const calendarEvents = calendarEventsData || [];
        
        // Initialize the calendar
        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,dayGridWeek,dayGridDay'
            },
            events: calendarEvents,
            eventClick: function(info) {
                showBookingDetails(info.event.id);
            },
            eventTimeFormat: {
                hour: 'numeric',
                minute: '2-digit',
                meridiem: 'short'
            },
            dayMaxEvents: true, // allow "more" link when too many events
            height: 'auto'
        });
        
        // Render the calendar
        calendar.render();
    }
    
    // Function to show booking details in a modal
    function showBookingDetails(bookingId) {
        // You can implement a modal to show booking details
        // This could be done via AJAX or by opening a new page
        window.location.href = '/bookings/' + bookingId;
    }
});