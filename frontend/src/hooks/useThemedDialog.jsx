import React, { useCallback, useRef, useState } from "react";
import ThemedDialog from "../components/ThemedDialog";

// Promise-based replacement for window.confirm / window.alert using the site's dialog theme.
// Render `dialog` once in the component, then: `if (!(await confirm({...}))) return;` or `await notify({...})`.
const useThemedDialog = () => {
  const [options, setOptions] = useState(null);
  const resolverRef = useRef(null);

  const close = useCallback((result) => {
    setOptions(null);
    const resolve = resolverRef.current;
    resolverRef.current = null;
    if (resolve) resolve(result);
  }, []);

  const open = useCallback((opts) => {
    // A dialog that is still open counts as cancelled, so its caller never hangs
    if (resolverRef.current) resolverRef.current(false);
    return new Promise((resolve) => {
      resolverRef.current = resolve;
      setOptions(opts);
    });
  }, []);

  // Resolves true on confirm, false on cancel or clicking outside
  const confirm = useCallback(
    (opts) => open({ confirmLabel: "Yes, Continue", cancelLabel: "Cancel", ...opts }),
    [open]
  );

  // Single-button notice for errors and hints; resolves when dismissed
  const notify = useCallback((opts) => open({ confirmLabel: "OK", ...opts, cancelLabel: undefined }), [open]);

  const dialog = (
    <ThemedDialog
      open={Boolean(options)}
      {...options}
      onConfirm={() => close(true)}
      onCancel={() => close(false)}
    />
  );

  return { dialog, confirm, notify };
};

export default useThemedDialog;
