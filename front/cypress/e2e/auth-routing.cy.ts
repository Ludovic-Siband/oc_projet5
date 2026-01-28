/// <reference types="cypress" />

describe('Protected routing', () => {
  const token = 'test-token';

  it('redirects to /login when unauthenticated', () => {
    cy.visit('/feed');
    cy.location('pathname').should('eq', '/login');

    cy.visit('/user');
    cy.location('pathname').should('eq', '/login');

    cy.visit('/subjects');
    cy.location('pathname').should('eq', '/login');

    cy.visit('/posts/create');
    cy.location('pathname').should('eq', '/login');
  });

  it('allows protected routes with a token', () => {
    cy.intercept('GET', '**/api/feed*', { fixture: 'feed.json' }).as('getFeed');

    cy.loginByToken(token, '/feed');

    cy.wait('@getFeed');
    cy.location('pathname').should('eq', '/feed');
  });
});
