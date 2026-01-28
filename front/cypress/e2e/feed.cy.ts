/// <reference types="cypress" />

describe('Feed', () => {
  const token = 'token-123';

  it('shows loading then the list', () => {
    cy.intercept('GET', '**/api/feed*', {
      delayMs: 300,
      fixture: 'feed.json',
    }).as('getFeed');

    cy.loginByToken(token, '/feed');

    cy.get('[data-cy="feed-loading"]').should('be.visible');
    cy.wait('@getFeed');
    cy.contains('Premier article').should('be.visible');
  });

  it('shows the empty state', () => {
    cy.intercept('GET', '**/api/feed*', { body: [] }).as('getFeed');

    cy.loginByToken(token, '/feed');

    cy.wait('@getFeed');
    cy.get('[data-cy="feed-empty"]').should('be.visible');
  });

  it('toggles sort and reloads', () => {
    const sorts: string[] = [];
    cy.intercept('GET', '**/api/feed*', (req) => {
      sorts.push(req.query.sort as string);
      req.reply({ fixture: 'feed.json' });
    }).as('getFeed');

    cy.loginByToken(token, '/feed');

    cy.wait('@getFeed');
    cy.get('[data-cy="feed-sort"]').click();
    cy.wait('@getFeed');

    cy.wrap(null).then(() => {
      expect(sorts.length).to.eq(2);
      expect(sorts[0]).to.be.a('string');
      expect(sorts[1]).to.be.a('string');
      expect(sorts[1]).to.not.eq(sorts[0]);
    });
  });

  it('opens a post detail', () => {
    cy.intercept('GET', '**/api/feed*', { fixture: 'feed.json' }).as('getFeed');
    cy.intercept('GET', '**/api/posts/1', { fixture: 'post.json' }).as('getPost');

    cy.loginByToken(token, '/feed');

    cy.wait('@getFeed');
    cy.get('[data-cy="feed-card-link-1"]').click();
    cy.wait('@getPost');
    cy.location('pathname').should('eq', '/feed/posts/1');
  });
});
