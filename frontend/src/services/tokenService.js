/**
 * Secure Token Storage Service
 * - Access token: stored in memory (protected from XSS)
 * - Refresh token: stored encrypted in IndexedDB (persistent)
 *
 * Note: The most secure approach is an httpOnly cookie set by the backend.
 * This implementation reduces exposure but cannot fully prevent XSS access.
 */

// In-memory storage for access token (most secure)
let accessToken = null;
let refreshToken = null;
let cryptoKey = null;
let initPromise = null;

// IndexedDB configuration
const DB_NAME = 'acm_secure_tokens';
const STORE_NAME = 'secure_store';
const REFRESH_TOKEN_ID = 'refresh_token';
const CRYPTO_KEY_ID = 'crypto_key';

/**
 * Open IndexedDB and ensure schema is available.
 */
const openDb = () => new Promise((resolve, reject) => {
  const request = indexedDB.open(DB_NAME, 1);

  request.onupgradeneeded = () => {
    const db = request.result;
    if (!db.objectStoreNames.contains(STORE_NAME)) {
      db.createObjectStore(STORE_NAME, { keyPath: 'id' });
    }
  };

  request.onsuccess = () => resolve(request.result);
  request.onerror = () => reject(request.error);
});

const requestToPromise = (request) => new Promise((resolve, reject) => {
  request.onsuccess = () => resolve(request.result);
  request.onerror = () => reject(request.error);
});

const waitForTransaction = (tx) => new Promise((resolve, reject) => {
  tx.oncomplete = () => resolve();
  tx.onerror = () => reject(tx.error);
  tx.onabort = () => reject(tx.error);
});

const getRecord = async (id) => {
  const db = await openDb();
  const tx = db.transaction(STORE_NAME, 'readonly');
  const store = tx.objectStore(STORE_NAME);
  const record = await requestToPromise(store.get(id));
  await waitForTransaction(tx);
  return record;
};

const setRecord = async (id, value) => {
  const db = await openDb();
  const tx = db.transaction(STORE_NAME, 'readwrite');
  const store = tx.objectStore(STORE_NAME);
  await requestToPromise(store.put({ id, value }));
  await waitForTransaction(tx);
};

const deleteRecord = async (id) => {
  const db = await openDb();
  const tx = db.transaction(STORE_NAME, 'readwrite');
  const store = tx.objectStore(STORE_NAME);
  await requestToPromise(store.delete(id));
  await waitForTransaction(tx);
};

const bufferToBase64 = (buffer) => {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  for (let i = 0; i < bytes.length; i += 1) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
};

const base64ToBuffer = (base64) => {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
};

const getCryptoKey = async () => {
  if (cryptoKey) {
    return cryptoKey;
  }

  const record = await getRecord(CRYPTO_KEY_ID);
  if (record && record.value) {
    cryptoKey = record.value;
    return cryptoKey;
  }

  const key = await crypto.subtle.generateKey(
    { name: 'AES-GCM', length: 256 },
    false,
    ['encrypt', 'decrypt']
  );

  await setRecord(CRYPTO_KEY_ID, key);
  cryptoKey = key;
  return cryptoKey;
};

const encryptText = async (text) => {
  const key = await getCryptoKey();
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const data = new TextEncoder().encode(text);
  const encrypted = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, data);

  return {
    iv: bufferToBase64(iv),
    data: bufferToBase64(encrypted),
  };
};

const decryptText = async (payload) => {
  if (!payload || !payload.iv || !payload.data) {
    return null;
  }

  const key = await getCryptoKey();
  const iv = new Uint8Array(base64ToBuffer(payload.iv));
  const encrypted = base64ToBuffer(payload.data);
  const decrypted = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, encrypted);

  return new TextDecoder().decode(decrypted);
};

/**
 * Initialize token service and load refresh token into memory.
 */
export const initializeTokenService = async () => {
  if (initPromise) {
    return initPromise;
  }

  initPromise = (async () => {
    try {
      const record = await getRecord(REFRESH_TOKEN_ID);
      if (record && record.value) {
        refreshToken = await decryptText(record.value);
      }
    } catch (error) {
      console.warn('Failed to initialize token storage:', error);
    }
  })();

  return initPromise;
};

/**
 * Access Token Management (in-memory)
 */
export const getAccessToken = () => accessToken;

export const setAccessToken = (token) => {
  accessToken = token;
};

export const removeAccessToken = () => {
  accessToken = null;
};

/**
 * Refresh Token Management (encrypted persistent storage)
 */
export const getRefreshToken = () => refreshToken;

export const loadRefreshToken = async () => {
  await initializeTokenService();
  return refreshToken;
};

export const setRefreshToken = async (token) => {
  refreshToken = token || null;

  try {
    if (!token) {
      await deleteRecord(REFRESH_TOKEN_ID);
      return;
    }

    const encrypted = await encryptText(token);
    await setRecord(REFRESH_TOKEN_ID, encrypted);
  } catch (error) {
    console.error('Failed to store refresh token:', error);
  }
};

export const removeRefreshToken = async () => {
  refreshToken = null;

  try {
    await deleteRecord(REFRESH_TOKEN_ID);
  } catch {
    // Ignore errors during cleanup
  }
};

/**
 * Set both tokens at once (e.g., after login)
 */
export const setTokens = async (access, refresh) => {
  setAccessToken(access);
  await setRefreshToken(refresh);
};

/**
 * Clear all tokens (logout)
 */
export const clearAllTokens = async () => {
  removeAccessToken();
  await removeRefreshToken();
};

/**
 * Check if user has valid access token
 */
export const hasAccessToken = () => !!accessToken;

/**
 * Check if user has refresh token cached in memory
 */
export const hasRefreshToken = () => !!refreshToken;

export default {
  initializeTokenService,
  getAccessToken,
  setAccessToken,
  removeAccessToken,
  getRefreshToken,
  loadRefreshToken,
  setRefreshToken,
  removeRefreshToken,
  setTokens,
  clearAllTokens,
  hasAccessToken,
  hasRefreshToken,
};