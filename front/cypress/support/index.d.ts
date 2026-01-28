declare namespace Cypress {
  interface Chainable {
    setAccessToken(token: string): Chainable<void>;
    loginByToken(token: string, path: string): Chainable<void>;
  }
}

export {};
