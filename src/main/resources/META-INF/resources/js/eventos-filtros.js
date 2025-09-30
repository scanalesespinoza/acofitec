(function () {
  function initFilters() {
    const tabButtons = Array.from(
      document.querySelectorAll('.events-tabs [data-tab]')
    );
    const panels = Array.from(
      document.querySelectorAll('.events-panel[data-panel]')
    );
    const searchInput = document.querySelector('[data-events-search]');

    if (!tabButtons.length || !panels.length) {
      return;
    }

    let activeTab =
      tabButtons.find((button) => button.classList.contains('is-active'))?.dataset.tab ||
      tabButtons[0].dataset.tab;

    function setActiveTab(tab) {
      activeTab = tab;
      tabButtons.forEach((button) => {
        const isCurrent = button.dataset.tab === activeTab;
        button.classList.toggle('is-active', isCurrent);
        button.setAttribute('aria-selected', String(isCurrent));
      });
    }

    function applyFilters() {
      const query = (searchInput?.value || '').trim().toLowerCase();

      panels.forEach((panel) => {
        const isActive = panel.dataset.panel === activeTab;
        panel.hidden = !isActive;
        panel.setAttribute('aria-hidden', String(!isActive));
        if (!isActive) {
          return;
        }

        const cards = Array.from(panel.querySelectorAll('[data-event-card]'));
        let visibleCards = 0;

        cards.forEach((card) => {
          const title = (card.dataset.title || '').toLowerCase();
          const description = (card.dataset.description || '').toLowerCase();
          const matches = !query || title.includes(query) || description.includes(query);
          card.style.display = matches ? '' : 'none';
          card.setAttribute('data-visible', matches ? 'true' : 'false');
          if (matches) {
            visibleCards += 1;
          }
        });

        const emptyMessages = panel.querySelectorAll('[data-empty]');
        emptyMessages.forEach((message) => {
          if (message.hasAttribute('hidden')) {
            message.hidden = visibleCards !== 0;
          }
        });
      });
    }

    tabButtons.forEach((button) => {
      button.addEventListener('click', () => {
        if (button.dataset.tab === activeTab) {
          return;
        }
        setActiveTab(button.dataset.tab);
        applyFilters();
      });
    });

    if (searchInput) {
      searchInput.addEventListener('input', () => {
        applyFilters();
      });
    }

    applyFilters();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initFilters);
  } else {
    initFilters();
  }
})();
