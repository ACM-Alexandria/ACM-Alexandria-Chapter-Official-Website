import { useState } from "react";
import { TbCheck, TbLink, TbLinkOff, TbX } from "react-icons/tb";

const ICON = { size: 18 };
const buttonClass =
    "flex h-8 w-8 items-center justify-center rounded text-slate-600 transition-colors hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-700";

const IconButton = ({ title, active = false, onClick, children }) => (
    <button
        type="button"
        title={title}
        aria-label={title}
        aria-pressed={active}
        onMouseDown={(event) => event.preventDefault()}
        onClick={onClick}
        className={`${buttonClass} ${active ? "bg-[#4B98C8]/20 text-[#205E85] dark:text-sky-300" : ""}`}
    >
        {children}
    </button>
);

const normalizeUrl = (raw) => {
    const value = raw.trim();
    if (!value) return null;
    if (/^(https?:\/\/|mailto:)/i.test(value)) return value;
    if (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return `mailto:${value}`;
    if (!/^[a-z][a-z0-9+.-]*:/i.test(value) && /^[^\s:/]+\.[^\s:/]+/.test(value)) return `https://${value}`;
    return null;
};

const LinkEditor = ({ editor, active }) => {
    const [open, setOpen] = useState(false);
    const [value, setValue] = useState("");
    const [error, setError] = useState("");

    const close = (refocus = true) => {
        setOpen(false);
        setError("");
        if (refocus) editor.commands.focus();
    };

    const toggle = () => {
        if (open) return close();
        setValue(editor.getAttributes("link").href || "");
        setError("");
        setOpen(true);
    };

    const apply = () => {
        const href = normalizeUrl(value);
        if (!href) {
            setError("Enter a valid web address or email, e.g. example.com");
            return;
        }

        const chain = editor.chain().focus();
        if (editor.state.selection.empty && !editor.isActive("link")) {
            chain.insertContent({ type: "text", text: value.trim(), marks: [{ type: "link", attrs: { href } }] }).run();
        } else {
            chain.extendMarkRange("link").setLink({ href }).run();
        }
        close(false);
    };

    const remove = () => {
        editor.chain().focus().extendMarkRange("link").unsetLink().run();
        close(false);
    };

    return (
        <>
            <div className="flex items-center gap-0.5 border-r border-slate-200 pr-1.5 last:border-r-0 dark:border-slate-700">
                <IconButton title="Add or edit link" active={active || open} onClick={toggle}>
                    <TbLink {...ICON} />
                </IconButton>
            </div>
            {open && (
                <div className="flex w-full flex-col gap-1 border-t border-slate-200 pt-1.5 dark:border-slate-700">
                    <div className="flex items-center gap-1.5">
                        <div className="relative flex-1">
                            <TbLink size={16} className="pointer-events-none absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" />
                            <input
                                type="text"
                                autoFocus
                                aria-label="Link address"
                                aria-invalid={Boolean(error)}
                                placeholder="Paste or type a link, e.g. example.com"
                                value={value}
                                onChange={(event) => {
                                    setValue(event.target.value);
                                    setError("");
                                }}
                                onKeyDown={(event) => {
                                    if (event.key === "Enter") {
                                        event.preventDefault();
                                        apply();
                                    } else if (event.key === "Escape") {
                                        event.preventDefault();
                                        close();
                                    }
                                }}
                                className={`h-8 w-full rounded border bg-white pl-8 pr-2 text-xs text-slate-700 outline-none dark:bg-slate-800 dark:text-slate-200 ${error
                                    ? "border-red-400 focus:border-red-500"
                                    : "border-slate-200 focus:border-[#4B98C8] dark:border-slate-600"
                                    }`}
                            />
                        </div>
                        <button
                            type="button"
                            onClick={apply}
                            className="flex h-8 items-center gap-1 rounded bg-[#4B98C8] px-3 text-xs font-medium text-white transition-colors hover:bg-[#3d86b3]"
                        >
                            <TbCheck size={16} />
                            {active ? "Update" : "Apply"}
                        </button>
                        {active && (
                            <IconButton title="Remove link" onClick={remove}>
                                <TbLinkOff {...ICON} />
                            </IconButton>
                        )}
                        <IconButton title="Cancel" onClick={() => close()}>
                            <TbX {...ICON} />
                        </IconButton>
                    </div>
                    {error && (
                        <p role="alert" className="px-1 text-xs text-red-500">
                            {error}
                        </p>
                    )}
                </div>
            )}
        </>
    );
};

export default LinkEditor;