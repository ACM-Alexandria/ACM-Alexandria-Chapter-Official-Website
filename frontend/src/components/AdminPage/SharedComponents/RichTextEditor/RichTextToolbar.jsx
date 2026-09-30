import { useEditorState } from "@tiptap/react";
import {
    TbAlignCenter,
    TbAlignJustified,
    TbAlignLeft,
    TbAlignRight,
    TbArrowBackUp,
    TbArrowForwardUp,
    TbBlockquote,
    TbBold,
    TbClearFormatting,
    TbItalic,
    TbList,
    TbListNumbers,
    TbMinus,
    TbTextColor,
    TbUnderline,
} from "react-icons/tb";
import LinkEditor from "./LinkEditor";

const ICON = { size: 18 };

const buttonClass =
    "flex h-8 w-8 items-center justify-center rounded text-slate-600 transition-colors hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent dark:text-slate-300 dark:hover:bg-slate-700";
const selectClass =
    "h-8 rounded border border-slate-200 bg-white px-2 text-xs text-slate-700 outline-none focus:border-[#4B98C8] dark:border-slate-600 dark:bg-slate-800 dark:text-slate-200";

const ToolbarButton = ({ title, active = false, disabled = false, onClick, children }) => (
    <button
        type="button"
        title={title}
        aria-label={title}
        aria-pressed={active}
        disabled={disabled}
        onMouseDown={(event) => event.preventDefault()}
        onClick={onClick}
        className={`${buttonClass} ${active ? "bg-[#4B98C8]/20 text-[#205E85] dark:text-sky-300" : ""}`}
    >
        {children}
    </button>
);

const ToolbarGroup = ({ children }) => (
    <div className="flex items-center gap-0.5 border-r border-slate-200 pr-1.5 last:border-r-0 dark:border-slate-700">
        {children}
    </div>
);

const ALIGNMENTS = [
    ["left", "Align left", TbAlignLeft],
    ["center", "Align center", TbAlignCenter],
    ["right", "Align right", TbAlignRight],
    ["justify", "Justify", TbAlignJustified],
];

const RichTextToolbar = ({ editor }) => {
    const state = useEditorState({
        editor,
        selector: ({ editor: e }) => {
            if (!e) return null;
            const textStyle = e.getAttributes("textStyle");
            return {
                bold: e.isActive("bold"),
                italic: e.isActive("italic"),
                underline: e.isActive("underline"),
                bulletList: e.isActive("bulletList"),
                orderedList: e.isActive("orderedList"),
                blockquote: e.isActive("blockquote"),
                link: e.isActive("link"),
                heading: [1, 2, 3].find((level) => e.isActive("heading", { level })) || "paragraph",
                textAlign: ALIGNMENTS.find(([alignment]) => e.isActive({ textAlign: alignment }))?.[0] || "left",
                fontFamily: textStyle.fontFamily || "",
                color: textStyle.color || "#000000",
                canUndo: e.can().undo(),
                canRedo: e.can().redo(),
            };
        },
    });
    if (!state) return null;

    const run = (command) => editor.chain().focus()[command]().run();

    // unsetAllMarks already removes color, font family and links
    const clearFormatting = () => editor.chain().focus().clearNodes().unsetAllMarks().setTextAlign("left").run();

    return (
        <div className="flex flex-wrap items-center gap-1 border-b border-slate-200 p-1.5 dark:border-slate-700">
            <ToolbarGroup>
                <select
                    aria-label="Block style"
                    title="Block style"
                    value={state.heading}
                    onChange={(event) => {
                        if (event.target.value === "paragraph") editor.chain().focus().setParagraph().run();
                        else editor.chain().focus().toggleHeading({ level: Number(event.target.value) }).run();
                    }}
                    className={selectClass}
                >
                    <option value="paragraph">Paragraph</option>
                    <option value={1}>Heading 1</option>
                    <option value={2}>Heading 2</option>
                    <option value={3}>Heading 3</option>
                </select>
                <select
                    aria-label="Font family"
                    title="Font family"
                    value={state.fontFamily}
                    onChange={(event) => {
                        if (event.target.value) editor.chain().focus().setFontFamily(event.target.value).run();
                        else editor.chain().focus().unsetFontFamily().run();
                    }}
                    className={selectClass}
                >
                    <option value="">Default font</option>
                    <option value="Arial, sans-serif">Arial</option>
                    <option value="Georgia, serif">Georgia</option>
                    <option value="'Times New Roman', serif">Times New Roman</option>
                    <option value="Verdana, sans-serif">Verdana</option>
                    <option value="'Courier New', monospace">Courier New</option>
                </select>
            </ToolbarGroup>

            <ToolbarGroup>
                <ToolbarButton title="Bold" active={state.bold} onClick={() => run("toggleBold")}>
                    <TbBold {...ICON} />
                </ToolbarButton>
                <ToolbarButton title="Italic" active={state.italic} onClick={() => run("toggleItalic")}>
                    <TbItalic {...ICON} />
                </ToolbarButton>
                <ToolbarButton title="Underline" active={state.underline} onClick={() => run("toggleUnderline")}>
                    <TbUnderline {...ICON} />
                </ToolbarButton>
                <label
                    title="Text color"
                    className="relative flex h-8 w-8 cursor-pointer items-center justify-center rounded text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-700"
                >
                    <TbTextColor {...ICON} />
                    <span
                        className="absolute bottom-1 left-2 right-2 h-[3px] rounded-full"
                        style={{ backgroundColor: state.color }}
                    />
                    <input
                        type="color"
                        aria-label="Text color"
                        value={state.color}
                        onChange={(event) => editor.chain().focus().setColor(event.target.value).run()}
                        className="absolute inset-0 h-full w-full cursor-pointer opacity-0"
                    />
                </label>
            </ToolbarGroup>

            <ToolbarGroup>
                <ToolbarButton title="Bulleted list" active={state.bulletList} onClick={() => run("toggleBulletList")}>
                    <TbList {...ICON} />
                </ToolbarButton>
                <ToolbarButton title="Numbered list" active={state.orderedList} onClick={() => run("toggleOrderedList")}>
                    <TbListNumbers {...ICON} />
                </ToolbarButton>
                <ToolbarButton title="Block quote" active={state.blockquote} onClick={() => run("toggleBlockquote")}>
                    <TbBlockquote {...ICON} />
                </ToolbarButton>
                <ToolbarButton title="Horizontal rule" onClick={() => run("setHorizontalRule")}>
                    <TbMinus {...ICON} />
                </ToolbarButton>
            </ToolbarGroup>

            <ToolbarGroup>
                {ALIGNMENTS.map(([alignment, title, Icon]) => (
                    <ToolbarButton
                        key={alignment}
                        title={title}
                        active={state.textAlign === alignment}
                        onClick={() => editor.chain().focus().setTextAlign(alignment).run()}
                    >
                        <Icon {...ICON} />
                    </ToolbarButton>
                ))}
            </ToolbarGroup>

            <LinkEditor editor={editor} active={state.link} />

            <ToolbarGroup>
                <ToolbarButton title="Undo" disabled={!state.canUndo} onClick={() => run("undo")}>
                    <TbArrowBackUp {...ICON} />
                </ToolbarButton>
                <ToolbarButton title="Redo" disabled={!state.canRedo} onClick={() => run("redo")}>
                    <TbArrowForwardUp {...ICON} />
                </ToolbarButton>
                <ToolbarButton title="Clear formatting" onClick={clearFormatting}>
                    <TbClearFormatting {...ICON} />
                </ToolbarButton>
            </ToolbarGroup>

        </div>
    );
};

export default RichTextToolbar;