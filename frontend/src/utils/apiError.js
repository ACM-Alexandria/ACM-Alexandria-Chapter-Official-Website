// A readable message for a failed API call: the server's `message`, a plain note for a bare 403
// (Spring Security sends no body), or the network error.
export const messageOf = (error) => {
  const message = error?.response?.data?.message;
  if (message) return message;
  if (error?.response?.status === 403) return "Your account doesn't have access to this.";
  return error?.message || "Something went wrong. Please try again.";
};
