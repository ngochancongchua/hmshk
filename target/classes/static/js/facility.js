// facility.js
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
    
    // Form validation for facility forms
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
    
    // Filter facilities by availability
    const availabilityFilter = document.getElementById('availabilityFilter');
    if (availabilityFilter) {
        availabilityFilter.addEventListener('change', function() {
            const value = this.value;
            const rows = document.querySelectorAll('tbody tr');
            
            rows.forEach(row => {
                const statusCell = row.querySelector('td:nth-child(7)');
                if (statusCell) {
                    const isAvailable = statusCell.textContent.trim() === 'Available';
                    
                    if (value === 'all' || 
                        (value === 'available' && isAvailable) || 
                        (value === 'unavailable' && !isAvailable)) {
                        row.style.display = '';
                    } else {
                        row.style.display = 'none';
                    }
                }
            });
        });
    }
});