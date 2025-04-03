// customer.js
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
    
    // Add event listeners for status filters
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            const value = this.value;
            const rows = document.querySelectorAll('tbody tr');
            
            rows.forEach(row => {
                const statusCell = row.querySelector('td:nth-child(6)');
                if (statusCell) {
                    const isActive = statusCell.querySelector('.badge.bg-success') !== null;
                    
                    if (value === 'all' || 
                        (value === 'active' && isActive) || 
                        (value === 'inactive' && !isActive)) {
                        row.style.display = '';
                    } else {
                        row.style.display = 'none';
                    }
                }
            });
        });
    }
    
    // Form validation for date fields
    const dateOfBirthInput = document.getElementById('dateOfBirth');
    if (dateOfBirthInput) {
        dateOfBirthInput.max = new Date().toISOString().split('T')[0]; // Set max date to today
    }
});