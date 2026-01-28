/// <reference types="cypress" />

describe('Post details', () => {
  const token = 'token-123';

  it('redirects when id is invalid', () => {
    cy.intercept('GET', '**/api/feed*', { fixture: 'feed.json' }).as('getFeed');

    cy.loginByToken(token, '/feed/posts/abc');

    cy.wait('@getFeed');
    cy.location('pathname').should('eq', '/feed');
  });

  it('shows the post and comments', () => {
    cy.intercept('GET', '**/api/posts/1', { fixture: 'post.json' }).as('getPost');

    cy.loginByToken(token, '/feed/posts/1');

    cy.wait('@getPost');
    cy.contains('Premier article').should('be.visible');
    cy.contains('Super post').should('be.visible');
  });

  it('validates the comment', () => {
    cy.intercept('GET', '**/api/posts/1', { fixture: 'post.json' }).as('getPost');

    cy.loginByToken(token, '/feed/posts/1');

    cy.wait('@getPost');
    cy.get('[data-cy="post-comment-content"]').focus().blur();
    cy.contains('Le commentaire est requis.').should('be.visible');
  });

  it('posts a comment and reloads', () => {
    cy.intercept('GET', '**/api/posts/1', { fixture: 'post.json' }).as('getPost');
    cy.intercept('POST', '**/api/posts/1/comments', { statusCode: 201, body: {} }).as('addComment');

    cy.loginByToken(token, '/feed/posts/1');

    cy.wait('@getPost');
    cy.get('[data-cy="post-comment-content"]').type('Nouveau commentaire');
    cy.get('[data-cy="post-comment-submit"]').click();

    cy.wait('@addComment');
    cy.wait('@getPost');
    cy.get('[data-cy="post-comment-content"]').should('have.value', '');
  });

  it('shows a server error', () => {
    cy.intercept('GET', '**/api/posts/1', { fixture: 'post.json' }).as('getPost');
    cy.intercept('POST', '**/api/posts/1/comments', {
      statusCode: 500,
      body: { message: 'Erreur serveur' },
    }).as('addComment');

    cy.loginByToken(token, '/feed/posts/1');

    cy.wait('@getPost');
    cy.get('[data-cy="post-comment-content"]').type('Nouveau commentaire');
    cy.get('[data-cy="post-comment-submit"]').click();

    cy.wait('@addComment');
    cy.contains('Erreur serveur').should('be.visible');
  });
});
