import { FiClock, FiArrowRight, FiFileText } from "react-icons/fi";

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
                            className="group relative bg-white rounded-[2rem] p-8 border border-slate-100/60 shadow-lg shadow-slate-200/40 hover:shadow-2xl hover:shadow-[#4B98C8]/20 transition-all duration-500 hover:-translate-y-2 flex flex-col cursor-pointer overflow-hidden"
                            data-aos="fade-up"
                            data-aos-delay={idx * 100}
                        >
                            {/* Animated Background Gradient on Hover */}
                            <div className="absolute inset-0 bg-gradient-to-br from-[#4B98C8]/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
                            
                            {/* Decorative accent line */}
                            <div className="absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r from-[#4B98C8] to-[#205E85] transform origin-left scale-x-0 group-hover:scale-x-100 transition-transform duration-500 ease-out" />

                            <div className="relative z-10 flex flex-col h-full">
                                {form.imageUrl ? (
                                    <div className="mb-6 -mx-8 -mt-8 h-48 overflow-hidden rounded-t-[2rem]">
                                        <img 
                                            src={form.imageUrl} 
                                            alt={form.title} 
                                            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" 
                                        />
                                    </div>
                                ) : (
                                    <div className="mb-6 -mx-8 -mt-8 h-48 overflow-hidden rounded-t-[2rem] bg-gradient-to-br from-[#4B98C8]/10 to-[#205E85]/5 flex items-center justify-center group-hover:from-[#4B98C8]/20 group-hover:to-[#205E85]/10 transition-colors duration-500">
                                        <FiFileText className="w-16 h-16 text-[#4B98C8]/40 group-hover:text-[#4B98C8]/70 group-hover:scale-110 transition-all duration-500" />
                                    </div>
                                )}
                                <h3 className="text-2xl font-extrabold text-slate-900 mb-4 line-clamp-2 leading-tight group-hover:text-[#205E85] transition-colors duration-300">
                                    {form.title}
                                </h3>
                                {form.description && (
                                    <p className="text-slate-500 text-base font-medium line-clamp-3 leading-relaxed mb-8 flex-grow">
                                        {form.description}
                                    </p>
                                )}
                                
                                <div className="mt-auto flex items-center justify-between text-[#4B98C8] font-bold text-sm uppercase tracking-widest pt-4 border-t border-slate-100 group-hover:border-[#4B98C8]/20 transition-colors duration-300">
                                    <span className="opacity-0 group-hover:opacity-100 transition-opacity duration-300 delay-100">View Details</span>
                                    <div className="w-10 h-10 rounded-full bg-slate-50 group-hover:bg-[#4B98C8]/10 flex items-center justify-center transition-all duration-300 transform group-hover:translate-x-2">
                                        <FiArrowRight className="w-5 h-5" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default ActiveFormsSection;
