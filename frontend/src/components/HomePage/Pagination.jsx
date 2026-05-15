const Pagination = ({ currentPage, totalPages, onPageChange }) => {
    if (totalPages <= 1) return null;

    // Build page number array with ellipsis logic
    const getPages = () => {
        const pages = [];
        const delta = 1; // pages around current
        const left = Math.max(0, currentPage - delta);
        const right = Math.min(totalPages - 1, currentPage + delta);

        if (left > 0) {
            pages.push(0);
            if (left > 1) pages.push("...");
        }

        for (let i = left; i <= right; i++) pages.push(i);

        if (right < totalPages - 1) {
            if (right < totalPages - 2) pages.push("...");
            pages.push(totalPages - 1);
        }

        return pages;
    };

    return (
        <div className="flex items-center justify-center gap-2 mt-10">
            {/* Prev */}
            <button
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="px-4 py-2 rounded-md text-sm font-semibold border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
                ← Prev
            </button>

            {/* Page numbers */}
            {getPages().map((page, idx) =>
                page === "..." ? (
                    <span key={`ellipsis-${idx}`} className="px-3 py-2 text-gray-400">
                        …
                    </span>
                ) : (
                    <button
                        key={page}
                        onClick={() => onPageChange(page)}
                        className={`w-10 h-10 rounded-md text-sm font-semibold transition-colors ${page === currentPage
                                ? "bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white shadow-md"
                                : "border border-gray-300 text-gray-700 hover:bg-gray-100"
                            }`}
                    >
                        {page + 1}
                    </button>
                )
            )}

            {/* Next */}
            <button
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
                className="px-4 py-2 rounded-md text-sm font-semibold border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
                Next →
            </button>
        </div>
    );
};

export default Pagination;
