/// <reference types="cypress" />

describe('User profile', () => {
  const token = 'token-123';

  it('shows profile and subscriptions', () => {
    cy.intercept('GET', '**/api/users/me', { delayMs: 200, fixture: 'user.json' }).as('getUser');

    cy.loginByToken(token, '/user');

    cy.contains('Chargement...').should('be.visible');
    cy.wait('@getUser');
    cy.get('[data-cy="user-username"]').should('have.value', 'user');
    cy.contains('Angular').should('be.visible');
  });

  it('shows a message when no fields are provided', () => {
    cy.intercept('GET', '**/api/users/me', { fixture: 'user.json' }).as('getUser');

    cy.loginByToken(token, '/user');

    cy.wait('@getUser');
    cy.get('[data-cy="user-username"]').clear();
    cy.get('[data-cy="user-email"]').clear();
    cy.get('[data-cy="user-password"]').clear();
    cy.get('[data-cy="user-submit"]').click();

    cy.contains('Veuillez renseigner au moins un champ.').should('be.visible');
  });

  it('updates the profile', () => {
    cy.intercept('GET', '**/api/users/me', { fixture: 'user.json' }).as('getUser');
    cy.intercept('PUT', '**/api/users/me', {
      statusCode: 200,
      body: { id: 5, email: 'new@test.fr', username: 'newuser' },
    }).as('updateUser');

    cy.loginByToken(token, '/user');

    cy.wait('@getUser');
    cy.get('[data-cy="user-username"]').clear().type('newuser');
    cy.get('[data-cy="user-email"]').clear().type('new@test.fr');
    cy.get('[data-cy="user-submit"]').click();

    cy.wait('@updateUser');
    cy.contains('Modification enregistrée').should('be.visible');
  });

  it('shows validation errors', () => {
    cy.intercept('GET', '**/api/users/me', { fixture: 'user.json' }).as('getUser');
    cy.intercept('PUT', '**/api/users/me', {
      statusCode: 400,
      body: { message: 'Erreur', fields: { email: 'Invalide' } },
    }).as('updateUser');

    cy.loginByToken(token, '/user');

    cy.wait('@getUser');
    cy.get('[data-cy="user-email"]').clear().type('bad@test.fr');
    cy.get('[data-cy="user-submit"]').click();

    cy.wait('@updateUser');
    cy.contains('Invalide').should('be.visible');
  });

  it('unsubscribes from a subject', () => {
    cy.intercept('GET', '**/api/users/me', { fixture: 'user.json' }).as('getUser');
    cy.intercept('DELETE', '**/api/subjects/10/subscribe', { statusCode: 200, body: {} }).as('unsubscribe');

    cy.loginByToken(token, '/user');

    cy.wait('@getUser');
    cy.get('[data-cy="user-unsubscribe-10"]').click();
    cy.wait('@unsubscribe');
    cy.contains('Angular').should('not.exist');
  });
});
