/**
 * @brief Toggle "View More" for any table with hidden rows.
 * @param {string} tableClass - The class of the table to toggle
 * @param {string} btnId - The ID of the button that toggles this table
 */
function toggleViewMore(tableClass, btnId) {
    const table = document.querySelector(`.${tableClass}`);
    const hiddenRows = table.querySelectorAll('tbody tr.hidden-row');
    const btn = document.getElementById(btnId);

    if (btn.textContent === 'View More') {
        hiddenRows.forEach(row => row.classList.remove('hidden-row'));
        btn.textContent = 'View Less';
    } else {
        hiddenRows.forEach(row => row.classList.add('hidden-row'));
        btn.textContent = 'View More';
        table.scrollIntoView({ behavior: 'smooth' });
    }
}