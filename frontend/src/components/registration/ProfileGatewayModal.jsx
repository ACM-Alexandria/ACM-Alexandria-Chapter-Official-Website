import { 
  HiOutlineX, 
  HiOutlineIdentification, 
  HiOutlineDeviceMobile, 
  HiOutlineAcademicCap, 
  HiOutlineBriefcase, 
  HiOutlineLibrary, 
  HiOutlineCheck,
  HiOutlineExclamationCircle,
  HiOutlineArrowRight
} from "react-icons/hi";

const ProfileGatewayModal = ({
  isVisible,
  onClose,
  entityName,
  profileForm,
  onInputChange,
  onToggle,
  onSubmit,
  submitting,
  error
}) => {

  // Subcomponent view mapping directly into Orchestrator stable backdrop
  return (
    <div className={`relative w-full max-w-2xl bg-white rounded-[2.5rem] shadow-2xl border border-slate-100 overflow-hidden transition-all duration-500 transform ease-[cubic-bezier(0.34,1.56,0.64,1)] flex flex-col max-h-[90vh] ${
      isVisible ? "translate-y-0 scale-100 opacity-100" : "translate-y-16 scale-95 opacity-0"
    }`}>
      
      {/* Sub Header */}
      <div className="bg-slate-50/50 px-8 py-6 border-b border-slate-100 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3.5">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-[#4B98C8] to-[#205E85] shadow-md flex items-center justify-center text-white shrink-0">
            <HiOutlineIdentification className="w-6 h-6" />
          </div>
          <div>
            <span className="text-[10px] font-black text-[#4B98C8] uppercase tracking-[0.15em] block">
              Step 1: Verify Profile
            </span>
            <h3 className="text-lg font-black text-slate-900 truncate leading-tight mt-0.5">
              {entityName}
            </h3>
          </div>
        </div>

        {!submitting && (
          <button onClick={onClose} className="w-10 h-10 rounded-xl bg-white border border-slate-200 text-slate-500 flex items-center justify-center transition-all hover:bg-slate-50 hover:text-slate-900 active:scale-95">
            <HiOutlineX className="w-5 h-5" />
          </button>
        )}
      </div>

      {/* Wizard Form Elements Scroll Space */}
      <div className="flex-1 overflow-y-auto p-8 md:p-10 scrollbar-hide">
        <form id="embedded-profile-gateway-form" onSubmit={onSubmit} className="space-y-6">
          
          <div className="bg-blue-50/60 border border-blue-100 rounded-2xl p-5 flex items-start gap-4">
            <HiOutlineIdentification className="w-6 h-6 text-[#4B98C8] shrink-0 mt-0.5" />
            <div>
              <h5 className="font-bold text-slate-800 text-sm leading-tight">Account Completion Required</h5>
              <p className="text-slate-500 font-medium text-[12px] mt-1 leading-relaxed">
                Please finalize your profile parameters. Your official name and valid phone number are required to validate and process your registration.
              </p>
            </div>
          </div>

          {error && (
            <div className="flex items-center gap-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-2xl px-5 py-4 animate-[fadeIn_0.3s_ease]">
              <HiOutlineExclamationCircle className="w-6 h-6 text-rose-500 shrink-0" />
              <p className="font-bold text-sm leading-snug">{error}</p>
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2 group">
              <label className="text-[13px] font-bold text-slate-800 flex items-center gap-2 ml-1">
                <HiOutlineIdentification className="w-4 h-4 text-slate-400" />
                Full Name <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                name="name"
                value={profileForm.name}
                onChange={onInputChange}
                required
                placeholder="Enter your full name"
                disabled={submitting}
                className="w-full h-14 px-5 bg-slate-50 border border-slate-100 rounded-2xl outline-none text-slate-800 font-semibold text-[15px] transition-all focus:border-[#4B98C8]/30 focus:bg-white focus:ring-4 focus:ring-blue-50"
              />
            </div>

            <div className="space-y-2 group">
              <label className="text-[13px] font-bold text-slate-800 flex items-center gap-2 ml-1">
                <HiOutlineDeviceMobile className="w-4 h-4 text-slate-400" />
                Phone Number <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                name="phoneNumber"
                value={profileForm.phoneNumber}
                onChange={onInputChange}
                required
                placeholder="e.g. 01XXXXXXXXX"
                maxLength={11}
                disabled={submitting}
                className="w-full h-14 px-5 bg-slate-50 border border-slate-100 rounded-2xl outline-none text-slate-800 font-semibold text-[15px] transition-all focus:border-[#4B98C8]/30 focus:bg-white focus:ring-4 focus:ring-blue-50"
              />
            </div>
          </div>

          <div className="bg-slate-50/50 border border-slate-100 rounded-2xl p-6 space-y-4">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-white border border-slate-100 flex items-center justify-center text-[#4B98C8]">
                  <HiOutlineLibrary className="w-5 h-5" />
                </div>
                <div>
                  <span className="text-xs font-bold text-slate-400 block mb-0.5">University Status</span>
                  <h5 className="font-extrabold text-slate-800 text-sm">Student in Faculty of Engineering? <span className="text-rose-500">*</span></h5>
                </div>
              </div>

              <div className="flex items-center gap-2 shrink-0">
                <button
                  type="button"
                  onClick={() => onToggle(true)}
                  disabled={submitting}
                  className={`px-5 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 ${
                    profileForm.isAlexEngStudent === true
                      ? "bg-[#4B98C8] text-white shadow-md" 
                      : "bg-white border border-slate-200 text-slate-500"
                  }`}
                >
                  {profileForm.isAlexEngStudent === true && <HiOutlineCheck className="w-3.5 h-3.5" />}
                  Yes
                </button>
                <button
                  type="button"
                  onClick={() => onToggle(false)}
                  disabled={submitting}
                  className={`px-5 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 ${
                    profileForm.isAlexEngStudent === false
                      ? "bg-slate-800 text-white shadow-md" 
                      : "bg-white border border-slate-200 text-slate-500"
                  }`}
                >
                  {profileForm.isAlexEngStudent === false && <HiOutlineCheck className="w-3.5 h-3.5" />}
                  No
                </button>
              </div>
            </div>

            {profileForm.isAlexEngStudent === true && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4 border-t border-slate-100 animate-[fadeIn_0.3s_ease]">
                <div className="space-y-2 group">
                  <label className="text-[13px] font-bold text-slate-800 flex items-center gap-2 ml-1">
                    <HiOutlineAcademicCap className="w-4 h-4 text-slate-400" />
                    Batch <span className="text-rose-500">*</span>
                  </label>
                  <div className="relative">
                    <select
                      name="batch"
                      value={profileForm.batch}
                      onChange={onInputChange}
                      required={profileForm.isAlexEngStudent === true}
                      disabled={submitting}
                      className="w-full h-12 px-4 bg-white border border-slate-100 rounded-xl outline-none text-slate-800 font-semibold text-sm appearance-none cursor-pointer transition-all focus:border-[#4B98C8]/30 focus:ring-4 focus:ring-blue-50"
                    >
                      <option value="">-- Select Batch --</option>
                      <option value="2026">2026</option>
                      <option value="2027">2027</option>
                      <option value="2028">2028</option>
                      <option value="2028++">2028++</option>
                      <option value="2029">2029</option>
                      <option value="2030">2030</option>
                    </select>
                    <div className="absolute inset-y-0 right-4 flex items-center pointer-events-none text-slate-400">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                      </svg>
                    </div>
                  </div>
                </div>

                <div className="space-y-2 group">
                  <label className="text-[13px] font-bold text-slate-800 flex items-center gap-2 ml-1">
                    <HiOutlineBriefcase className="w-4 h-4 text-slate-400" />
                    Department <span className="text-rose-500">*</span>
                  </label>
                  <div className="relative">
                    <select
                      name="department"
                      value={profileForm.department}
                      onChange={onInputChange}
                      required={profileForm.isAlexEngStudent === true}
                      disabled={submitting}
                      className="w-full h-12 px-4 bg-white border border-slate-100 rounded-xl outline-none text-slate-800 font-semibold text-sm appearance-none cursor-pointer transition-all focus:border-[#4B98C8]/30 focus:ring-4 focus:ring-blue-50"
                    >
                      <option value="">-- Select Department --</option>
                      <option value="CSED">Computer & Systems (CSED)</option>
                      <option value="CCE">Computer & Communications (CCE)</option>
                      <option value="OTHER">Other</option>
                    </select>
                    <div className="absolute inset-y-0 right-4 flex items-center pointer-events-none text-slate-400">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                      </svg>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </form>
      </div>

      {/* Sticky Action Footer */}
      <div className="bg-slate-50/30 px-8 py-6 border-t border-slate-100 flex justify-end gap-3 shrink-0">
        <button type="button" onClick={onClose} disabled={submitting} className="px-7 py-3.5 rounded-xl border border-slate-200 text-slate-600 text-sm font-bold transition-all hover:bg-white hover:text-slate-800 hover:shadow-sm disabled:opacity-50">
          Cancel
        </button>
        <button
          type="submit"
          form="embedded-profile-gateway-form"
          disabled={submitting}
          className="group px-8 py-3.5 flex items-center justify-center gap-2 bg-gradient-to-r from-slate-800 to-slate-900 text-white text-sm font-bold rounded-xl shadow-md shadow-slate-300 hover:shadow-lg hover:-translate-y-0.5 transition-all active:scale-95 disabled:opacity-70"
        >
          {submitting ? "Verifying..." : "Save & Continue"}
          <HiOutlineArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform ml-1" />
        </button>
      </div>
    </div>
  );
};

export default ProfileGatewayModal;
