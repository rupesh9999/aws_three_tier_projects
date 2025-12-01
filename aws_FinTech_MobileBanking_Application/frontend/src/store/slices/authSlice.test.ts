import { describe, it, expect } from 'vitest';
import authReducer, {
  setCredentials,
  logout,
  setLoading,
  setError,
  clearError,
} from './authSlice';

describe('authSlice', () => {
  const initialState = {
    user: null,
    token: null,
    refreshToken: null,
    isAuthenticated: false,
    isLoading: false,
    error: null,
    mfaRequired: false,
    mfaToken: null,
  };

  it('should return the initial state', () => {
    expect(authReducer(undefined, { type: 'unknown' })).toEqual(initialState);
  });

  describe('setCredentials', () => {
    it('should set user credentials and authentication state', () => {
      const user = {
        id: '123',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
      };
      const token = 'test-token';
      const refreshToken = 'test-refresh-token';

      const actual = authReducer(
        initialState,
        setCredentials({ user, token, refreshToken })
      );

      expect(actual.user).toEqual(user);
      expect(actual.token).toEqual(token);
      expect(actual.refreshToken).toEqual(refreshToken);
      expect(actual.isAuthenticated).toBe(true);
      expect(actual.error).toBeNull();
    });
  });

  describe('logout', () => {
    it('should clear all auth state', () => {
      const authenticatedState = {
        ...initialState,
        user: { id: '123', email: 'test@example.com' },
        token: 'test-token',
        isAuthenticated: true,
      };

      const actual = authReducer(authenticatedState, logout());

      expect(actual.user).toBeNull();
      expect(actual.token).toBeNull();
      expect(actual.refreshToken).toBeNull();
      expect(actual.isAuthenticated).toBe(false);
    });
  });

  describe('setLoading', () => {
    it('should set loading state', () => {
      const actual = authReducer(initialState, setLoading(true));
      expect(actual.isLoading).toBe(true);

      const actual2 = authReducer(actual, setLoading(false));
      expect(actual2.isLoading).toBe(false);
    });
  });

  describe('setError', () => {
    it('should set error message', () => {
      const errorMessage = 'Invalid credentials';
      const actual = authReducer(initialState, setError(errorMessage));
      expect(actual.error).toBe(errorMessage);
    });
  });

  describe('clearError', () => {
    it('should clear error message', () => {
      const stateWithError = { ...initialState, error: 'Some error' };
      const actual = authReducer(stateWithError, clearError());
      expect(actual.error).toBeNull();
    });
  });
});
