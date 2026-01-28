/// <reference types="cypress" />

describe('Subjects', () => {
  const token = 'token-123';

  it('shows loading then subjects', () => {
    cy.intercept('GET', '**/api/subjects', { delayMs: 300, fixture: 'subjects.json' }).as('getSubjects');

    cy.loginByToken(token, '/subjects');

    cy.get('[data-cy="subjects-loading"]').should('be.visible');
    cy.wait('@getSubjects');
    cy.contains('Angular').should('be.visible');
  });

  it('subscribes a user', () => {
    cy.intercept('GET', '**/api/subjects', { fixture: 'subjects.json' }).as('getSubjects');
    cy.intercept('POST', '**/api/subjects/10/subscribe', { statusCode: 200, body: {} }).as('subscribe');

    cy.loginByToken(token, '/subjects');

    cy.wait('@getSubjects');
    cy.get('[data-cy="subject-subscribe-10"]').click();
    cy.wait('@subscribe');
    cy.contains('h2', 'Angular').closest('mat-card').contains('Déjà abonné').should('be.visible');
  });

  it('shows an error when subscribing', () => {
    cy.intercept('GET', '**/api/subjects', { fixture: 'subjects.json' }).as('getSubjects');
    cy.intercept('POST', '**/api/subjects/10/subscribe', {
      statusCode: 409,
      body: { message: 'Deja abonne' },
    }).as('subscribe');

    cy.loginByToken(token, '/subjects');

    cy.wait('@getSubjects');
    cy.get('[data-cy="subject-subscribe-10"]').click();
    cy.wait('@subscribe');
    cy.contains('Deja abonne').should('be.visible');
  });
});
