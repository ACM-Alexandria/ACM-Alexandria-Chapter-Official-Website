import React, { useEffect, useMemo } from "react";
import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import Color from "@tiptap/extension-color";
import FontFamily from "@tiptap/extension-font-family";
import Link from "@tiptap/extension-link";
import Placeholder from "@tiptap/extension-placeholder";
import TextAlign from "@tiptap/extension-text-align";
import { TextStyle } from "@tiptap/extension-text-style";
import Underline from "@tiptap/extension-underline";
import { EMAIL_CONTENT_CLASSES } from "../../../../constants/emailContent";
import RichTextToolbar from "./RichTextToolbar";
import "./RichTextEditor.css";

const RichTextEditor = ({ value = "", onChange, placeholder = "Write your email..." }) => {
    const extensions = useMemo(
        () => [
            StarterKit.configure({
                heading: { levels: [1, 2, 3] },
                link: false,
                underline: false,
                strike: false,
                code: false,
                codeBlock: false,
            }),
            TextStyle,
            Color,
            FontFamily,
            Underline,
            Link.configure({
                openOnClick: false,
                autolink: true,
                HTMLAttributes: { target: "_blank", rel: "noopener noreferrer nofollow" },
            }),
            Placeholder.configure({ placeholder }),
            TextAlign.configure({ types: ["heading", "paragraph"] }),
        ],
        [placeholder]
    );

    const editor = useEditor({
        immediatelyRender: false,
        extensions,
        content: value,
        editorProps: {
            attributes: {
                class: `email-rich-text-content min-h-[220px] px-3 py-2.5 text-sm text-slate-900 outline-none dark:text-white ${EMAIL_CONTENT_CLASSES}`,
            },
        },
        onUpdate: ({ editor: updatedEditor }) => {
            onChange(updatedEditor.isEmpty ? "" : updatedEditor.getHTML());
        },
    });

    useEffect(() => {
        if (!editor || (!value && editor.isEmpty) || editor.getHTML() === value) return;
        editor.commands.setContent(value || "", { emitUpdate: false });
    }, [editor, value]);

    return (
        <div className="email-rich-text-editor overflow-hidden rounded-md border border-slate-300 bg-white focus-within:border-[#4B98C8] focus-within:ring-2 focus-within:ring-[#4B98C8]/20 dark:border-slate-600 dark:bg-slate-800">
            <RichTextToolbar editor={editor} />
            <EditorContent editor={editor} />
        </div>
    );
};

export default RichTextEditor;