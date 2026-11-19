// Sends a signed-out visitor to log in, bringing them back to this page with `param` set so the same
// panel or application reopens.
export const goToLogin = (navigate, param, id) => {
  const path = window.location.pathname + window.location.search;
  const from = `${path}${path.includes("?") ? "&" : "?"}${param}=${id}`;
  navigate("/login", { state: { from } });
};
