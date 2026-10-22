import React from "react";
import { HiOutlineExclamation, HiOutlineX } from "react-icons/hi";

const ConfirmDeleteModal = ({ isOpen, onClose, onConfirm, itemName }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-xl dark:shadow-slate-950/60 max-w-sm w-full p-6 animate-[scaleIn_0.25s_ease] relative">

                {/* Close Button */}
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 dark:text-slate-300 hover:text-slate-600 dark:hover:text-slate-100 rounded-lg transition-colors"
                >
                    <HiOutlineX className="w-5 h-5" />
                </button>

                {/* Content */}
                <div className="flex flex-col items-center text-center mt-2">
                    <div className="w-12 h-12 rounded-full bg-rose-100 dark:bg-rose-900/30 flex items-center justify-center mb-4">
                        <HiOutlineExclamation className="w-6 h-6 text-rose-600 dark:text-rose-400" />
                    </div>
                    <h3 className="text-lg font-extrabold text-slate-800 dark:text-slate-100 mb-2">
                        Delete Points Log
                    </h3>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mb-6">
                        Are you sure you want to delete <span className="font-bold text-slate-700 dark:text-slate-300">"{itemName}"</span>? This action cannot be undone and will permanently deduct the points from the user.
                    </p>
                </div>

                {/* Action Buttons */}
                <div className="flex items-center gap-3 w-full">
                    <button
                        onClick={onClose}
                        className="flex-1 py-2.5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600 text-slate-600 dark:text-slate-300 rounded-xl text-xs font-bold tracking-wide transition-all active:scale-95"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={onConfirm}
                        className="flex-1 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold tracking-wide shadow-md shadow-rose-600/20 transition-all active:scale-95"
                    >
                        Delete
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmDeleteModal;