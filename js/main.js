/* ==========================================================================
   DEVELOPER WEBPAGE INTERACTION LOGIC - VECTRA LABS
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
  
  // 1. Mobile Menu Toggle
  const menuToggle = document.getElementById('menuToggle');
  const navMenu = document.getElementById('navMenu');
  
  if (menuToggle && navMenu) {
    menuToggle.addEventListener('click', () => {
      const isActive = navMenu.classList.toggle('active');
      menuToggle.textContent = isActive ? '✕' : '☰';
    });
    
    // Close mobile menu when clicking a link
    navMenu.querySelectorAll('a').forEach(link => {
      link.addEventListener('click', () => {
        navMenu.classList.remove('active');
        menuToggle.textContent = '☰';
      });
    });
  }

  // 2. Apps Catalog Filter (Homepage Only)
  const filterButtons = document.querySelectorAll('.filter-btn');
  const appCards = document.querySelectorAll('.app-card');
  
  if (filterButtons.length > 0 && appCards.length > 0) {
    filterButtons.forEach(button => {
      button.addEventListener('click', () => {
        // Toggle active button class
        filterButtons.forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');
        
        const filterValue = button.getAttribute('data-filter');
        
        // Filter cards
        appCards.forEach(card => {
          const category = card.getAttribute('data-category');
          if (filterValue === 'all' || category === filterValue) {
            card.style.display = 'flex';
            // Trigger a quick fade-in animation
            card.style.opacity = '0';
            setTimeout(() => {
              card.style.transition = 'opacity 0.4s ease';
              card.style.opacity = '1';
            }, 50);
          } else {
            card.style.display = 'none';
          }
        });
      });
    });
  }

  // 3. Form Submission Handlers (Support Page Only)
  const supportForm = document.getElementById('supportForm');
  const deletionForm = document.getElementById('deletionForm');
  
  // Support Form Handler
  if (supportForm) {
    supportForm.addEventListener('submit', (e) => {
      e.preventDefault();
      
      if (validateForm(supportForm)) {
        const name = document.getElementById('supportName').value;
        const app = document.getElementById('supportApp').value;
        
        // Simulate API call success
        showToast(`Thank you, ${name}! Your support request for ${formatAppName(app)} has been submitted. Check your email shortly.`, 'success');
        supportForm.reset();
        
        // Remove validation classes
        removeValidationStyles(supportForm);
      } else {
        showToast('Please correct the highlighted fields before submitting.', 'error');
      }
    });
  }
  
  // Deletion Form Handler
  if (deletionForm) {
    deletionForm.addEventListener('submit', (e) => {
      e.preventDefault();
      
      const confirmCheck = document.getElementById('deleteConfirm');
      if (!confirmCheck.checked) {
        showToast('You must confirm the deletion consent checkbox to proceed.', 'error');
        confirmCheck.classList.add('form-input-error');
        return;
      }
      
      if (validateForm(deletionForm)) {
        const name = document.getElementById('deleteName').value;
        const email = document.getElementById('deleteEmail').value;
        const app = document.getElementById('deleteApp').value;
        
        // Simulate API call success
        showToast(`Data deletion request submitted successfully for ${name} (${email}) under app ${formatAppName(app)}. Our system will delete this data within 7 business days.`, 'success', 8000);
        deletionForm.reset();
        
        // Remove validation classes
        removeValidationStyles(deletionForm);
      } else {
        showToast('Please correct the highlighted fields before submitting.', 'error');
      }
    });
  }
  
  // Form Validation helper
  function validateForm(form) {
    let isValid = true;
    const requiredInputs = form.querySelectorAll('[required]');
    
    requiredInputs.forEach(input => {
      if (input.type === 'checkbox') {
        if (!input.checked) {
          isValid = false;
        }
      } else if (!input.value.trim()) {
        isValid = false;
      } else if (input.type === 'email') {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(input.value)) {
          isValid = false;
        }
      }
    });
    
    return isValid;
  }
  
  // Clean validation visual states after successful submit
  function removeValidationStyles(form) {
    form.querySelectorAll('.form-input, .form-textarea, .form-select').forEach(input => {
      input.blur();
    });
  }
  
  // Helper to format app selector name
  function formatAppName(appValue) {
    const appMap = {
      'ai-pdf-master': 'AI PDF Master',
      'secret-video-recorder': 'Secret Video Recorder',
      'voiceforge-ai': 'VoiceForge AI',
      'free-ai-art-generator': 'Free AI Art Generator'
    };
    return appMap[appValue] || appValue;
  }

  // 4. Toast Notification Creator
  function showToast(message, type = 'success', duration = 5000) {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    // Add dynamic icon based on toast type
    const icon = type === 'success' ? '✓' : '⚠';
    toast.innerHTML = `<span>${icon}</span> <div>${message}</div>`;
    
    container.appendChild(toast);
    
    // Auto-remove toast
    setTimeout(() => {
      toast.style.animation = 'slideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) reverse forwards';
      setTimeout(() => {
        toast.remove();
      }, 300);
    }, duration);
  }
});
