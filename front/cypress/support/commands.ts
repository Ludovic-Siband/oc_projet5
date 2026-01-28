Cypress.Commands.add('setAccessToken', (token: string) => {
  cy.window().then((win) => {
    win.localStorage.setItem('access_token', token);
  });
});

Cypress.Commands.add('loginByToken', (token: string, path: string) => {
  cy.visit('/');
  cy.setAccessToken(token);
  cy.visit(path);
});
