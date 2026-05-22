declare global {
  interface Window {
    ATLAS_CONFIG?: {
      apiUrl?: string;
    };
  }
}

export const environment = {
  apiUrl: window.ATLAS_CONFIG?.apiUrl?.replace(/\/+$/, '') || 'http://localhost:8091'
};
