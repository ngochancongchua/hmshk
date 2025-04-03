// Booking page functionality

document.addEventListener('DOMContentLoaded', function() {
    // Add date validation for booking dates
    const checkInDateInput = document.getElementById('checkInDate');
    const checkOutDateInput = document.getElementById('checkOutDate');
    
    if (checkInDateInput && checkOutDateInput) {
        checkInDateInput.addEventListener('change', function() {
            // Set minimum checkout date to check-in date
            checkOutDateInput.min = this.value;
            
            // If checkout date is before new check-in date, update it
            if (checkOutDateInput.value && new Date(checkOutDateInput.value) < new Date(this.value)) {
                checkOutDateInput.value = this.value;
            }
        });
        
        // Calculate total price when dates or room change
        const calculatePrice = function() {
            const roomSelect = document.getElementById('roomId');
            if (checkInDateInput.value && checkOutDateInput.value && roomSelect.value) {
                const selectedOption = roomSelect.options[roomSelect.selectedIndex];
                const pricePerNight = parseFloat(selectedOption.textContent.match(/\$(\d+(\.\d+)?)/)[1]);
                const checkIn = new Date(checkInDateInput.value);
                const checkOut = new Date(checkOutDateInput.value);
                const nights = Math.floor((checkOut - checkIn) / (1000 * 60 * 60 * 24));
                
                const totalPriceElement = document.getElementById('totalPrice');
                if (totalPriceElement) {
                    totalPriceElement.textContent = `$${(pricePerNight * nights).toFixed(2)}`;
                }
            }
        };
        
        checkInDateInput.addEventListener('change', calculatePrice);
        checkOutDateInput.addEventListener('change', calculatePrice);
        if (document.getElementById('roomId')) {
            document.getElementById('roomId').addEventListener('change', calculatePrice);
        }
    }
});