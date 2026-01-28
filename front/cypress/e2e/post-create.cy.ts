/// <reference types="cypress" />

describe('Post creation', () => {
  const token = 'token-123';

  it('loads subjects', () => {
    cy.intercept('GET', '**/api/subjects', { delayMs: 300, fixture: 'subjects.json' }).as('getSubjects');

    cy.loginByToken(token, '/posts/create');

    cy.wait('@getSubjects');
    cy.get('mat-select[formcontrolname="subjectId"]').should('not.be.disabled');
  });

  it('validates required fields', () => {
    cy.intercept('GET', '**/api/subjects', { fixture: 'subjects.json' }).as('getSubjects');

    cy.loginByToken(token, '/posts/create');

    cy.wait('@getSubjects');
    cy.get('form').submit();
    cy.contains('Le thème est requis.').should('be.visible');
    cy.contains("Le titre est requis.").should('be.visible');
    cy.contains('Le contenu est requis.').should('be.visible');
  });

  it('creates a post and redirects', () => {
    cy.intercept('GET', '**/api/subjects', { fixture: 'subjects.json' }).as('getSubjects');
    cy.intercept('POST', '**/api/posts', { statusCode: 201, body: { id: 42 } }).as('createPost');
    cy.intercept('GET', '**/api/posts/42', { fixture: 'post.json' }).as('getPost');

    cy.loginByToken(token, '/posts/create');

    cy.wait('@getSubjects');
    cy.get('[data-cy="post-create-subject"]')
      .should('have.attr', 'aria-disabled', 'false')
      .click({ force: true });
    cy.get('[data-cy="post-create-option-11"]').should('be.visible').click();
    cy.get('[data-cy="post-create-title"]').click({ force: true }).type('Nouveau post', { force: true });
    cy.get('[data-cy="post-create-content"]').click({ force: true }).type('Contenu', { force: true });
    cy.get('[data-cy="post-create-submit"]').click();

    cy.wait('@createPost');
    cy.wait('@getPost');
    cy.location('pathname').should('eq', '/feed/posts/42');
  });

  it('shows server errors', () => {
    cy.intercept('GET', '**/api/subjects', { fixture: 'subjects.json' }).as('getSubjects');
    cy.intercept('POST', '**/api/posts', {
      statusCode: 400,
      body: { message: 'Erreur', fields: { title: 'Invalide' } },
    }).as('createPost');

    cy.loginByToken(token, '/posts/create');

    cy.wait('@getSubjects');
    cy.get('[data-cy="post-create-subject"]')
      .should('have.attr', 'aria-disabled', 'false')
      .click({ force: true });
    cy.get('[data-cy="post-create-option-11"]').should('be.visible').click();
    cy.get('[data-cy="post-create-title"]').click({ force: true }).type('Nouveau post', { force: true });
    cy.get('[data-cy="post-create-content"]').click({ force: true }).type('Contenu', { force: true });
    cy.get('[data-cy="post-create-submit"]').click();

    cy.wait('@createPost');
    cy.contains('Invalide').should('be.visible');
  });
});
