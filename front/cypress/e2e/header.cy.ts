/// <reference types="cypress" />

describe('Header navigation and logout', () => {
  const token = 'token-123';

  beforeEach(() => {
    cy.intercept('GET', '**/api/feed*', { fixture: 'feed.json' }).as('getFeed');
    cy.loginByToken(token, '/feed');
    cy.wait('@getFeed');
  });

  it('navigates via header links', () => {
    cy.intercept('GET', '**/api/subjects', { fixture: 'subjects.json' }).as('getSubjects');

    cy.get('[data-cy="header-burger"]').click();
    cy.get('[data-cy="header-drawer-subjects"]').click();
    cy.wait('@getSubjects');
    cy.location('pathname').should('eq', '/subjects');

    cy.intercept('GET', '**/api/users/me', { fixture: 'user.json' }).as('getUser');
    cy.get('[data-cy="header-burger"]').click();
    cy.get('[data-cy="header-drawer-user"]').click();
    cy.wait('@getUser');
    cy.location('pathname').should('eq', '/user');
  });

  it('opens and closes the burger menu', () => {
    cy.get('button[aria-label="Menu"]').click();
    cy.get('.app-nav__drawer').should('be.visible');

    cy.get('.app-nav__overlay').click();
    cy.get('.app-nav__drawer').should('not.exist');
  });

  it('logout redirects to /', () => {
    cy.intercept('POST', '**/api/auth/logout', {
      statusCode: 200,
      body: {},
    }).as('logout');

    cy.get('[data-cy="header-burger"]').click();
    cy.get('[data-cy="header-drawer-logout"]').click();
    cy.wait('@logout');
    cy.location('pathname').should('eq', '/');
  });
});
