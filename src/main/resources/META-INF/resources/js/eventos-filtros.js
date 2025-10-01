(function () {
  function ready(fn) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', fn);
    } else {
      fn();
    }
  }

  ready(function () {
    var tabButtons = Array.prototype.slice.call(
      document.querySelectorAll('[data-tab-target]')
    );
    var panels = Array.prototype.slice.call(
      document.querySelectorAll('[data-tab-panel]')
    );
    var searchInput = document.querySelector('[data-event-search]');

    if (tabButtons.length === 0 || panels.length === 0) {
      return;
    }

    var activeTab = (function () {
      var active = tabButtons.find(function (button) {
        return button.classList.contains('is-active');
      });
      return active
        ? active.getAttribute('data-tab-target')
        : tabButtons[0].getAttribute('data-tab-target');
    })();

    function setActiveTab(target) {
      if (!target) {
        return;
      }
      activeTab = target;
      tabButtons.forEach(function (button) {
        var isActive = button.getAttribute('data-tab-target') === target;
        button.classList.toggle('is-active', isActive);
        button.setAttribute('aria-selected', String(isActive));
        button.setAttribute('tabindex', isActive ? '0' : '-1');
      });

      panels.forEach(function (panel) {
        var isActive = panel.getAttribute('data-tab-panel') === target;
        panel.classList.toggle('is-hidden', !isActive);
        panel.setAttribute('aria-hidden', String(!isActive));
      });

      filterPanels();
    }

    function filterPanels() {
      var query = '';
      if (searchInput && typeof searchInput.value === 'string') {
        query = searchInput.value.trim().toLowerCase();
      }

      panels.forEach(function (panel) {
        var cards = Array.prototype.slice.call(
          panel.querySelectorAll('.event-card')
        );
        var searchMessage = panel.querySelector('[data-search-empty]');

        if (cards.length === 0) {
          if (searchMessage) {
            searchMessage.classList.add('is-hidden');
          }
          return;
        }

        var visibleCount = 0;
        cards.forEach(function (card) {
          var title = card.getAttribute('data-title') || '';
          var description = card.getAttribute('data-description') || '';
          var matches = query.length === 0;

          if (!matches) {
            var combined = (title + ' ' + description).toLowerCase();
            matches = combined.indexOf(query) !== -1;
          }

          card.classList.toggle('is-hidden', !matches);
          if (matches) {
            visibleCount += 1;
          }
        });

        if (searchMessage) {
          searchMessage.classList.toggle('is-hidden', visibleCount !== 0);
        }
      });
    }

    tabButtons.forEach(function (button, index) {
      button.addEventListener('click', function () {
        setActiveTab(button.getAttribute('data-tab-target'));
        button.focus();
      });

      button.addEventListener('keydown', function (event) {
        if (event.key !== 'ArrowRight' && event.key !== 'ArrowLeft') {
          return;
        }
        event.preventDefault();
        var direction = event.key === 'ArrowRight' ? 1 : -1;
        var nextIndex = (index + direction + tabButtons.length) % tabButtons.length;
        var nextButton = tabButtons[nextIndex];
        if (nextButton) {
          setActiveTab(nextButton.getAttribute('data-tab-target'));
          nextButton.focus();
        }
      });
    });

    if (searchInput) {
      searchInput.addEventListener('input', function () {
        filterPanels();
      });
    }

    setActiveTab(activeTab);
  });
})();
