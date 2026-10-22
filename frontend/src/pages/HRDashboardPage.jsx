import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FiUsers, FiMail, FiSliders, FiLayers, FiCalendar, FiAward, FiBookOpen, FiFileText, FiAlertTriangle, FiLock, FiRefreshCw } from 'react-icons/fi';

// Import Components
import Navbar from '../components/HomePage/Navbar';
import ManagementSidebar from '../components/AdminPage/ManagementSection/ManagementSidebar';
import UserManagementTab from '../components/AdminPage/ManagementSection/UserManagementTab';
import ResourceTable from '../components/AdminPage/ManagementSection/ResourceTable';
import EmailCenterTab from '../components/AdminPage/EmailCenterSection/EmailCenterTab';
import AdminPageHeader from '../components/AdminPage/SharedComponents/AdminPageHeader';
import AdminTabs from '../components/AdminPage/SharedComponents/AdminTabs';
import ResourceToolbar from '../components/AdminPage/SharedComponents/ResourceToolbar';
import { noticeClass } from '../components/AdminPage/SharedComponents/adminStyles';
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


const HRDashboardPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const { dialog } = useThemedDialog();

    // Top-level tabs (Horizontal)
    const [activeTopTab, setActiveTopTab] = useState('resources');
    const [searchQuery, setSearchQuery] = useState("");

    // Sidebar tabs (Vertical - Inside Resource Management)
    const [activeResourceTab, setActiveResourceTab] = useState('users');

    // --- EMAIL LOCK STATES ---
    const [emailsEnabled, setEmailsEnabled] = useState(true);

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
        { id: 'resources', label: 'Resource Management', shortLabel: 'Resources', icon: FiSliders },
        { id: 'emails', label: 'Email Center', shortLabel: 'Email', icon: FiMail }
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
    // Call email message editor (same behaviour as the admin page)
    const [messageSubject, setMessageSubject] = useState("");
    const [messageBody, setMessageBody] = useState("");
    const [messageSaving, setMessageSaving] = useState(false);
    const [modalError, setModalError] = useState(null);

    const handleEditMessageClick = (committee) => {
        setSelectedResource(committee);
        setMessageSubject(committee.callMessage?.subject || "");
        setMessageBody(committee.callMessage?.body || "");
        setModalError(null);
        setIsCallMessageModalOpen(true);
    };

    const handleMessageSubmit = async (e) => {
        e.preventDefault();
        setMessageSaving(true);
        setModalError(null);
        try {
            await adminService.changeCallMessage(selectedResource.id, { subject: messageSubject, body: messageBody });
            setIsCallMessageModalOpen(false);
            setSelectedResource(null);
            await loadResourceData();
        } catch (err) {
            console.error(err);
            setModalError(err.message || err.error || "Failed to update call message.");
        } finally {
            setMessageSaving(false);
        }
    };

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
                onEditMessageClick={handleEditMessageClick}

                // Ignored in HR mode (No permissions)
                onEditClick={() => { }}
                onDeleteClick={() => { }}
                onToggleCall={() => { }}
            />
        );
    };

    const activeSection = RESOURCE_TABS.find((tab) => tab.id === activeResourceTab);

    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-slate-100 font-sans">
            <Navbar activeSection="" />

            <main className="pt-[106px] pb-20 px-4 sm:px-6 md:px-12 max-w-[1600px] mx-auto flex flex-col gap-6">
                <AdminPageHeader title="Dashboard" description="Human Resources" />

                {!emailsEnabled && (
                    <div role="alert" className={`flex items-start gap-2.5 ${noticeClass.error}`}>
                        <FiAlertTriangle className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />
                        <p>
                            All site emails are locked. No emails or notifications are sent (except password resets).
                            {" Please contact the HR Board to unlock them."}
                        </p>
                    </div>
                )}

                <AdminTabs tabs={hrTabs} active={activeTopTab} onChange={setActiveTopTab} ariaLabel="HR sections" />

                {activeTopTab === 'resources' ? (
                    <div className="flex flex-col lg:flex-row gap-4 lg:gap-6 items-start">
                        <ManagementSidebar
                            activeTab={activeResourceTab}
                            setActiveTab={setActiveResourceTab}
                            tabs={RESOURCE_TABS}
                            setSearchQuery={setSearchQuery}
                            isHRMode={HRMode}
                        />

                        <div className="flex-1 w-full min-w-0 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-4 sm:p-6 min-h-[500px]">
                            {activeResourceTab !== 'users' && (
                                <ResourceToolbar
                                    title={activeSection?.label || 'Resources'}
                                    description="Browse registrations, questions and emails for this section."
                                    search={searchQuery}
                                    onSearchChange={setSearchQuery}
                                />
                            )}
                            {renderResourceContent()}
                        </div>
                    </div>
                ) : (
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

            <CallMessageModal
                open={isCallMessageModalOpen}
                onClose={() => { setIsCallMessageModalOpen(false); setModalError(null); }}
                onSubmit={handleMessageSubmit}
                selectedCommittee={selectedResource}
                messageSubject={messageSubject}
                setMessageSubject={setMessageSubject}
                messageBody={messageBody}
                setMessageBody={setMessageBody}
                loading={messageSaving}
                error={modalError}
            />

            {/* REQUIRED for the confirm/notify dialogs to render */}
            {dialog}
        </div>
    );
};

export default HRDashboardPage;