import { FiClock } from "react-icons/fi";

const ActiveFormsSection = ({ loading, activeForms, onShowDetails }) => {
    if (loading) {
        return (
            <div className="mt-24 w-full relative overflow-hidden" id="exclusive-forms">
                <div className="max-w-7xl mx-auto space-y-6 animate-pulse">
                    <div className="h-12 bg-slate-100 rounded-full w-80 mx-auto" />
                    <div className="h-4 bg-slate-100 rounded-full w-64 mx-auto mt-4" />
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10 mt-16">
                        {[1, 2, 3].map(i => (
                            <div key={i} className="h-48 bg-slate-50 rounded-[2rem] border border-slate-100" />
                        ))}
                    </div>
                </div>
            </div>
        );
    }

    if (!activeForms || activeForms.length === 0) {
        return null;
    }

    return (
        <div className="mt-24 w-full relative overflow-hidden" id="exclusive-forms">
            <div className="max-w-7xl mx-auto relative z-10">
                {/* Header Section */}
                <div className="text-center mb-20">
                    <h2 
                        className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight mb-6 flex items-center justify-center gap-3"
                        data-aos="fade-up"
                    >
                        Exclusive <span className="text-[#205E85]">Opportunities</span>
                        <FiClock className="w-8 h-8 md:w-10 md:h-10 text-[#4B98C8]" />
                    </h2>
                    <p
                        className="text-slate-500 text-lg max-w-2xl mx-auto font-medium"
                        data-aos="fade-up"
                        data-aos-delay="100"
                    >
                        Don't miss out on these limited-time, highly exclusive registrations. Secure your spot now and elevate your journey.
                    </p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
                    {activeForms.map((form, idx) => (
                        <div 
                            key={form.id}
                            onClick={() => onShowDetails(form.id)}
                            className="group relative bg-white rounded-[2rem] p-8 border border-slate-100 shadow-xl shadow-slate-200/50 hover:shadow-2xl hover:shadow-slate-300/50 transition-all duration-300 hover:-translate-y-2 flex flex-col cursor-pointer"
                            data-aos="fade-up"
                            data-aos-delay={idx * 100}
                        >
                            <h3 className="text-2xl font-extrabold text-slate-900 mb-4 line-clamp-2 leading-tight group-hover:text-[#205E85] transition-colors">
                                {form.title}
                            </h3>
                            {form.description && (
                                <p className="text-slate-500 text-base font-medium line-clamp-3 leading-relaxed mb-0">
                                    {form.description}
                                </p>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default ActiveFormsSection;
