/**
 * @brief Submits a form via AJAX and removes it from browser history.
 *        Prevents normal form submission, sends the form data via fetch,
 *        replaces the current history entry so pressing Back skips the form,
 *        and navigates to the specified success page.
 *
 * @param {string} formId - The ID of the form element to submit.
 * @param {string} successUrl - The URL to navigate to on successful submission.
 * @param {string} [fallbackUrl="/"] - Optional URL to use when replacing the current history entry.
 */
function submitForm(formId, successUrl, fallbackUrl = "/") {
    const form = document.getElementById(formId);
    if (!form) {
        console.error(`[submitForm] Form with ID "${formId}" not found.`);
        return;
    }

    form.addEventListener('submit', async function(event) {
        event.preventDefault(); // prevent normal form submission

        const formData = new FormData(form);
        const actionUrl = form.action;

        try {
            // Submit form via fetch
            const response = await fetch(actionUrl, {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (response.ok) {
                // Replace current form page in history so Back skips it
                history.replaceState({}, "", fallbackUrl);

                // Navigate to success page without adding a history entry
                window.location.replace(successUrl);

            } else {
                // Show server-side errors inline
                const text = await response.text();
                const errorMsg = form.querySelector('#errorMsg') || document.getElementById('errorMsg');
                if (errorMsg) errorMsg.textContent = text;
                else console.error("[submitForm] Error message element not found:", text);
            }
        } catch (err) {
            console.error("[submitForm] Form submission failed:", err);
            const errorMsg = form.querySelector('#errorMsg') || document.getElementById('errorMsg');
            if (errorMsg) errorMsg.textContent = "An error occurred. Please try again.";
        }
    });
}

/**
 * @brief Attach submitForm to multiple forms easily.
 *        Pass an array of form configs: { formId, successUrl, fallbackUrl? }
 *
 * @param {Array} forms - Array of form configuration objects.
 */
function attachForms(forms) {
    if (!Array.isArray(forms)) return;

    forms.forEach(cfg => {
        submitForm(cfg.formId, cfg.successUrl, cfg.fallbackUrl || "/");
    });
}