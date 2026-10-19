import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { HiOutlineArrowLeft } from 'react-icons/hi';
import { FiUsers, FiMail, FiSliders, FiLayers, FiCalendar, FiAward, FiBookOpen, FiFileText, FiAlertTriangle, FiLock, FiRefreshCw } from 'react-icons/fi';

// Import Components
import Navbar from '../components/HomePage/Navbar';
import ManagementSidebar from '../components/AdminPage/ManagementSection/ManagementSidebar';
import UserManagementTab from '../components/AdminPage/ManagementSection/UserManagementTab';
import ResourceTable from '../components/AdminPage/ManagementSection/ResourceTable';
import EmailCenterTab from '../components/AdminPage/EmailCenterSection/EmailCenterTab';
import HRFolderIcon from '../components/icons/HRFolderIcon';
import { Highlight } from "../components/ThemedDialog";
import useThemedDialog from "../hooks/useThemedDialog";

// Import API Services
import { fetchCommittee, fetchEvents, fetchClubs, fetchPrograms } from '../services/homePageService';
import adminService from '../services/adminService';

// Import Modals
import RegistrationPanelModal from '../components/AdminPage/ManagementSection/RegistrationPanelModal';
import QuestionsManagementModal from '../components/AdminPage/ManagementSection/QuestionsManagementModal';
import RegistrationEmailsModal from '../components/AdminPage/ManagementSection/RegistrationEmailsModal';
import CallMessageModal from '../components/AdminPage/ManagementSection/CallMessageModal';

/* ─── Brand Colors ─── */
const B = "#4B98C8";
const BD = "#205E85";

const HRDashboardPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const { dialog, confirm, notify } = useThemedDialog();

    const isSuperAdmin = user?.role === "SUPER_ADMIN";

    // Top-level tabs (Horizontal)
    const [activeTopTab, setActiveTopTab] = useState('resources');
    const [searchQuery, setSearchQuery] = useState("");

    // Sidebar tabs (Vertical - Inside Resource Management)
    const [activeResourceTab, setActiveResourceTab] = useState('users');

    // --- EMAIL LOCK STATES ---
    const [emailsEnabled, setEmailsEnabled] = useState(true);
    const [emailLockSaving, setEmailLockSaving] = useState(false);
    const emailLockInFlight = useRef(false);

    // --- RESOURCE DATA STATES ---
    const [committees, setCommittees] = useState([]);
    const [events, setEvents] = useState([]);
    const [clubs, setClubs] = useState([]);
    const [programs, setPrograms] = useState([]);
    const [exclusiveForms, setExclusiveForms] = useState([]);
    const [filteredItems, setFilteredItems] = useState([]);
    const [isLoadingResources, setIsLoadingResources] = useState(false);

    // --- MODAL STATES ---
    const [selectedResource, setSelectedResource] = useState(null);
    const [isRegistrationModalOpen, setIsRegistrationModalOpen] = useState(false);
    const [isQuestionsModalOpen, setIsQuestionsModalOpen] = useState(false);
    const [isEmailsModalOpen, setIsEmailsModalOpen] = useState(false);
    const [isCallMessageModalOpen, setIsCallMessageModalOpen] = useState(false);

    // HR Dashboard Main Tabs
    const hrTabs = [
        { id: 'resources', label: 'Resource Management', icon: <FiSliders className="w-4 h-4" /> },
        { id: 'emails', label: 'Email Center', icon: <FiMail className="w-4 h-4" /> }
    ];

    const RESOURCE_TABS = [
        { id: 'users', label: 'Users', icon: FiUsers },
        { id: 'committees', label: 'Committees', icon: FiLayers },
        { id: 'events', label: 'Events', icon: FiCalendar },
        { id: 'clubs', label: 'Clubs', icon: FiAward },
        { id: 'programs', label: 'Programs', icon: FiBookOpen },
        { id: 'exclusiveForms', label: 'Exclusive Forms', icon: FiFileText },
    ];

    // check user role & committee
    const isSuperOrHighBoard = ['SUPER_ADMIN', 'ACM_HIGH_BOARD'].includes(user.role);
    const isHRCommittee = user.committeeName && user.committeeName === 'HR Committee';

    const isHRBoard = user.role === 'ACM_COMMITTEE_BOARD' && isHRCommittee;
    const isHRMember = user.role === 'ACM_MEMBER' && isHRCommittee;
    const HRMode = isHRCommittee || isHRBoard || isSuperOrHighBoard;

    // Security Guard: Ensure only authorized roles/committees are here
    useEffect(() => {
        if (!user) return;

        if (!isSuperOrHighBoard && !isHRBoard && !isHRMember) {
            navigate('/', { replace: true });
        }
    }, [user, navigate]);

    // Fetch Email Lock Status
    useEffect(() => {
        adminService.fetchEmailLock()
            .then((data) => setEmailsEnabled(data?.emailsEnabled !== false))
            .catch((err) => console.error("Failed to load email lock status", err));
    }, []);


    // Fetch data from backend
    const loadResourceData = async () => {
        setIsLoadingResources(true);
        try {
            const [cData, eData, clData, pData, fData] = await Promise.all([
                fetchCommittee(),
                fetchEvents(0, 100),
                fetchClubs(0, 100),
                fetchPrograms(0, 100),
                adminService.fetchExclusiveForms() // Fixed!
            ]);

            setCommittees(cData || []);
            setEvents(eData?.content || []);
            setClubs(clData?.content || []);
            setPrograms(pData?.content || []);
            setExclusiveForms(fData || []); // Extracted directly, no `.content` needed
        } catch (err) {
            console.error("Failed to fetch HR resources:", err);
        } finally {
            setIsLoadingResources(false);
        }
    };

    // Trigger fetch on load
    useEffect(() => {
        if (user) loadResourceData();
    }, [user]);

    // Handle Search and Tab Filtering
    useEffect(() => {
        let activeData = [];
        switch (activeResourceTab) {
            case 'committees': activeData = committees; break;
            case 'events': activeData = events; break;
            case 'clubs': activeData = clubs; break;
            case 'programs': activeData = programs; break;
            case 'exclusiveForms': activeData = exclusiveForms; break;
            default: activeData = [];
        }

        if (!searchQuery) {
            setFilteredItems(activeData);
        } else {
            const lowerQ = searchQuery.toLowerCase();
            setFilteredItems(activeData.filter(item =>
                (item.name || item.title || "").toLowerCase().includes(lowerQ)
            ));
        }
    }, [activeResourceTab, searchQuery, committees, events, clubs, programs, exclusiveForms]);

    if (!user) return null;

    const renderResourceContent = () => {
        if (activeResourceTab === 'users') {
            return <UserManagementTab hideManageAccess={HRMode} />;
        }

        return (
            <ResourceTable
                activeTab={activeResourceTab}
                filteredItems={filteredItems}
                loading={isLoadingResources}
                isHRMode={HRMode}

                // Attach Modal Handlers
                onRegistrationClick={(item) => { setSelectedResource(item); setIsRegistrationModalOpen(true); }}
                onQuestionsClick={(item) => { setSelectedResource(item); setIsQuestionsModalOpen(true); }}
                onEmailsClick={(item) => { setSelectedResource(item); setIsEmailsModalOpen(true); }}
                onEditMessageClick={(item) => { setSelectedResource(item); setIsCallMessageModalOpen(true); }}

                // Ignored in HR mode (No permissions)
                onEditClick={() => { }}
                onDeleteClick={() => { }}
                onToggleCall={() => { }}
            />
        );
    };

    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-slate-100 font-sans">
            <Navbar activeSection="" />

            {/* Exactly matches the sizing from AdminPage */}
            <main className="pt-[100px] pb-20 px-4 sm:px-6 md:px-10 lg:px-14 max-w-[1400px] mx-auto">

                {/* 1. Back Button */}
                <button
                    onClick={() => navigate(-1)}
                    className="mb-8 inline-flex items-center gap-2 px-4 py-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl text-xs font-bold text-slate-600 dark:text-slate-300 shadow-sm hover:bg-slate-50 dark:hover:bg-slate-800 transition-all"
                >
                    <HiOutlineArrowLeft className="w-4 h-4" /> Back
                </button>

                {/* ── Header ── */}
                <div
                    className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-8"
                    style={{ animation: "fadeIn 0.5s ease both" }}
                >
                    <div>
                        <div className="flex items-center gap-2 mb-1">
                            <HRFolderIcon className="w-5 h-5" style={{ color: B }} />
                            <span className="text-[10px] font-bold uppercase tracking-[0.2em]" style={{ color: B }}>
                                Human Resources
                            </span>
                        </div>
                        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 dark:text-slate-100 tracking-tight">
                            Dashboard
                        </h1>
                    </div>
                </div>

                {/* ── Email lock warning ── */}
                {!emailsEnabled && (
                    <div className="flex items-center gap-3 p-4 mb-8 bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-200 rounded-xl">
                        <FiAlertTriangle className="w-5 h-5 shrink-0" />
                        <p className="text-xs font-bold">
                            All site emails are locked. No emails or notifications are sent (except password resets).
                            {false ? " Use the Site Emails switch above to unlock." : " Please contact the HR Board to unlock them."}
                        </p>
                    </div>
                )}

                {/* 3. Horizontal Tab Switcher */}
                <div className="flex gap-1 bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-700 p-1 rounded-xl w-fit shadow-sm dark:shadow-slate-950/40 mb-8">
                    {hrTabs.map(tab => (
                        <button
                            key={tab.id}
                            onClick={() => setActiveTopTab(tab.id)}
                            className={`flex items-center gap-2 px-5 py-2.5 rounded-lg text-xs font-bold uppercase tracking-wider transition-all duration-200 ${activeTopTab === tab.id
                                ? "text-white shadow-md"
                                : "text-slate-500 dark:text-slate-300 hover:text-slate-700 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-800"
                                }`}
                            style={activeTopTab === tab.id ? { background: `linear-gradient(135deg, ${B}, ${BD})` } : {}}
                        >
                            {tab.icon}
                            {tab.label}
                        </button>
                    ))}
                </div>

                {/* 4. Tab Content Layout */}
                {activeTopTab === 'resources' ? (
                    /* The flex wrapper was fixed here to match AdminPage precisely! */
                    <div className="flex flex-col lg:flex-row gap-8 items-start animate-[fadeIn_0.4s_ease]">
                        {/* Left Sidebar Menu (Wrapper removed so it renders identically) */}
                        <ManagementSidebar
                            activeTab={activeResourceTab}
                            setActiveTab={setActiveResourceTab}
                            tabs={RESOURCE_TABS}
                            setSearchQuery={setSearchQuery}
                            isHRMode={HRMode}
                        />

                        {/* Right Content Tables */}
                        <div className="flex-1 w-full bg-white dark:bg-slate-900 rounded-2xl border border-slate-200/80 dark:border-slate-700 p-6 shadow-sm dark:shadow-slate-950/40 min-h-[500px] flex flex-col justify-between">
                            {renderResourceContent()}
                        </div>
                    </div>
                ) : (
                    /* No borders for EmailCenterTab, exactly like the AdminPage! */
                    <EmailCenterTab />
                )}

            </main>

            {/* --- RENDER MODALS --- */}
            {isRegistrationModalOpen && selectedResource && (
                <RegistrationPanelModal
                    open={isRegistrationModalOpen}
                    onClose={() => { setIsRegistrationModalOpen(false); setSelectedResource(null); }}
                    resourceId={selectedResource.id}
                    resourceType={activeResourceTab === 'exclusiveForms' ? 'exclusive-form' : activeResourceTab}
                    resourceName={selectedResource.name || selectedResource.title}
                />
            )}

            {isQuestionsModalOpen && selectedResource && (
                <QuestionsManagementModal
                    open={isQuestionsModalOpen}
                    onClose={() => { setIsQuestionsModalOpen(false); setSelectedResource(null); }}
                    resourceId={selectedResource.id}
                    resourceType={activeResourceTab === 'exclusiveForms' ? 'exclusive-form' : activeResourceTab}
                    resourceName={selectedResource.name || selectedResource.title}
                />
            )}

            {isEmailsModalOpen && selectedResource && (
                <RegistrationEmailsModal
                    open={isEmailsModalOpen}
                    onClose={() => { setIsEmailsModalOpen(false); setSelectedResource(null); }}
                    resource={selectedResource}
                    resourceType={activeResourceTab === 'exclusiveForms' ? 'exclusive-form' : activeResourceTab}
                    resourceName={selectedResource.name || selectedResource.title}
                />
            )}

            {isCallMessageModalOpen && selectedResource && (
                <CallMessageModal
                    open={isCallMessageModalOpen}
                    onClose={() => { setIsCallMessageModalOpen(false); setSelectedResource(null); }}
                    committee={selectedResource}
                    onSaveSuccess={loadResourceData}
                />
            )}

            {/* REQUIRED for the confirm/notify dialogs to render */}
            {dialog}
        </div>
    );
};

export default HRDashboardPage;