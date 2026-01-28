/// <reference types="cypress" />

describe('Auth interceptor', () => {
  it('adds the bearer token to API requests', () => {
    cy.intercept('GET', '**/api/feed*', (req) => {
      expect(req.headers.authorization).to.eq('Bearer token-abc');
      req.reply({ fixture: 'feed.json' });
    }).as('getFeed');

    cy.loginByToken('token-abc', '/feed');

    cy.wait('@getFeed');
  });

  it('refreshes the token after 401 and retries the request', () => {
    let callCount = 0;
    const authHeaders: string[] = [];

    cy.intercept('GET', '**/api/feed*', (req) => {
      authHeaders.push(req.headers.authorization as string);
      if (callCount === 0) {
        callCount += 1;
        req.reply({ statusCode: 401, body: { message: 'Unauthorized' } });
        return;
      }

      req.reply({ fixture: 'feed.json' });
    }).as('getFeed');

    cy.intercept('POST', '**/api/auth/refresh', {
      statusCode: 200,
      body: { accessToken: 'new-token' },
    }).as('refresh');

    cy.loginByToken('old-token', '/feed');

    cy.wait('@refresh');
    cy.wait('@getFeed').its('response.statusCode').should('eq', 401);

    cy.window().then((win) => {
      expect(win.localStorage.getItem('access_token')).to.eq('new-token');
    });

    cy.wrap(null).then(() => {
      expect(authHeaders[0]).to.eq('Bearer old-token');
      if (authHeaders.length > 1) {
        expect(authHeaders[1]).to.eq('Bearer new-token');
      }
    });
  });

  it('redirects to /login when refresh fails', () => {
    cy.intercept('GET', '**/api/feed*', { statusCode: 401, body: { message: 'Unauthorized' } }).as('getFeed');
    cy.intercept('POST', '**/api/auth/refresh', { statusCode: 401, body: {} }).as('refresh');

    cy.loginByToken('old-token', '/feed');

    cy.wait('@refresh');
    cy.location('pathname').should('eq', '/login');
  });
});
