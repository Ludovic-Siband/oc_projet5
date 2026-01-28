/// <reference types="cypress" />

describe('Home, Login, Register', () => {
  it('navigates from home', () => {
    cy.visit('/');
    cy.get('[data-cy="home-login"]').click();
    cy.location('pathname').should('eq', '/login');

    cy.visit('/');
    cy.get('[data-cy="home-register"]').click();
    cy.location('pathname').should('eq', '/register');
  });

  it('shows login validation errors', () => {
    cy.visit('/login');
    cy.get('form').submit();
    cy.contains("L'identifiant est requis.").should('be.visible');
    cy.contains('Le mot de passe est requis.').should('be.visible');
  });

  it('logs in and redirects to /feed', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: {
        accessToken: 'token-123',
        user: { id: 1, email: 'user@test.fr', username: 'user' },
      },
    }).as('login');

    cy.intercept('GET', '**/api/feed*', { fixture: 'feed.json' }).as('getFeed');

    cy.visit('/login');
    cy.get('[data-cy="login-identifier"]').click({ force: true }).type('user', { force: true });
    cy.get('[data-cy="login-password"]').click({ force: true }).type('Password1!', { force: true });
    cy.get('[data-cy="login-submit"]').click();

    cy.wait('@login');
    cy.wait('@getFeed');
    cy.location('pathname').should('eq', '/feed');
    cy.window().then((win) => {
      expect(win.localStorage.getItem('access_token')).to.eq('token-123');
    });
  });

  it('shows field errors on login 400', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 400,
      body: {
        message: 'Erreur de validation',
        fields: { identifier: 'Invalide', password: 'Invalide' },
      },
    }).as('login');

    cy.visit('/login');
    cy.get('[data-cy="login-identifier"]').click({ force: true }).type('user', { force: true });
    cy.get('[data-cy="login-password"]').click({ force: true }).type('bad', { force: true });
    cy.get('[data-cy="login-submit"]').click();

    cy.wait('@login');
    cy.contains('Invalide').should('be.visible');
  });

  it('shows snackbar on login 401', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 401,
      body: { message: 'Identifiants invalides' },
    }).as('login');

    cy.visit('/login');
    cy.get('[data-cy="login-identifier"]').click({ force: true }).type('user', { force: true });
    cy.get('[data-cy="login-password"]').click({ force: true }).type('bad', { force: true });
    cy.get('[data-cy="login-submit"]').click();

    cy.wait('@login');
    cy.contains('Identifiants invalides').should('be.visible');
  });

  it('registers and redirects to /login', () => {
    cy.intercept('POST', '**/api/auth/register', {
      statusCode: 201,
      body: { id: 1, email: 'user@test.fr', username: 'user' },
    }).as('register');

    cy.visit('/register');
    cy.get('[data-cy="register-username"]').click({ force: true }).type('user', { force: true });
    cy.get('[data-cy="register-email"]').click({ force: true }).type('user@test.fr', { force: true });
    cy.get('[data-cy="register-password"]').click({ force: true }).type('Password1!', { force: true });
    cy.get('[data-cy="register-submit"]').click();

    cy.wait('@register');
    cy.location('pathname').should('eq', '/login');
  });

  it('shows field errors on register 400', () => {
    cy.intercept('POST', '**/api/auth/register', {
      statusCode: 400,
      body: {
        message: 'Erreur',
        fields: { username: 'Invalide', email: 'Invalide' },
      },
    }).as('register');

    cy.visit('/register');
    cy.get('[data-cy="register-username"]').click({ force: true }).type('user', { force: true });
    cy.get('[data-cy="register-email"]').click({ force: true }).type('user@test.fr', { force: true });
    cy.get('[data-cy="register-password"]').click({ force: true }).type('Password1!', { force: true });
    cy.get('[data-cy="register-submit"]').click();

    cy.wait('@register');
    cy.contains('Invalide').should('be.visible');
  });

  it('shows snackbar on register 409', () => {
    cy.intercept('POST', '**/api/auth/register', {
      statusCode: 409,
      body: { message: 'Deja utilise' },
    }).as('register');

    cy.visit('/register');
    cy.get('[data-cy="register-username"]').click({ force: true }).type('user', { force: true });
    cy.get('[data-cy="register-email"]').click({ force: true }).type('user@test.fr', { force: true });
    cy.get('[data-cy="register-password"]').click({ force: true }).type('Password1!', { force: true });
    cy.get('[data-cy="register-submit"]').click();

    cy.wait('@register');
    cy.contains('Deja utilise').should('be.visible');
  });
});
