import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FiMail, FiSliders, FiAlertTriangle } from 'react-icons/fi';

import Navbar from '../components/HomePage/Navbar';
import ManagementSidebar from '../components/AdminPage/ManagementSection/ManagementSidebar';
import UserManagementTab from '../components/AdminPage/ManagementSection/UserManagementTab';
import ResourceTable from '../components/AdminPage/ManagementSection/ResourceTable';
import ResourceDialogs from '../components/AdminPage/ManagementSection/ResourceDialogs';
import EmailCenterTab from '../components/AdminPage/EmailCenterSection/EmailCenterTab';
import AdminPageHeader from '../components/AdminPage/SharedComponents/AdminPageHeader';
import AdminTabs from '../components/AdminPage/SharedComponents/AdminTabs';
import ResourceToolbar from '../components/AdminPage/SharedComponents/ResourceToolbar';
import ResourcePagination from '../components/AdminPage/SharedComponents/ResourcePagination';
import { noticeClass } from '../components/AdminPage/SharedComponents/adminStyles';

import useResourceList from '../hooks/useResourceList';
import useResourceDialogs from '../hooks/useResourceDialogs';
import useEmailLock from '../hooks/useEmailLock';
import { SHARED_RESOURCE_TABS, resourceTypeForTab } from '../constants/resourceTypes';

const hrTabs = [
    { id: 'resources', label: 'Resource Management', shortLabel: 'Resources', icon: FiSliders },
    { id: 'emails', label: 'Email Center', shortLabel: 'Email', icon: FiMail }
];

const HRDashboardPage = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    // Top-level tabs (Horizontal)
    const [activeTopTab, setActiveTopTab] = useState('resources');
    const [searchQuery, setSearchQuery] = useState("");

    // Sidebar tabs (Vertical - Inside Resource Management)
    const [activeResourceTab, setActiveResourceTab] = useState('users');

    const [emailsEnabled] = useEmailLock();
    const resources = useResourceList(activeResourceTab);
    const dialogs = useResourceDialogs({ onChanged: resources.reload });

    // check user role & committee
    const isSuperOrHighBoard = ['SUPER_ADMIN', 'ACM_HIGH_BOARD'].includes(user?.role);
    const isHRCommittee = user?.committeeName && user.committeeName === 'HR Committee';

    const isHRBoard = user?.role === 'ACM_COMMITTEE_BOARD' && isHRCommittee;
    const isHRMember = user?.role === 'ACM_MEMBER' && isHRCommittee;
    const HRMode = isHRCommittee || isHRBoard || isSuperOrHighBoard;

    // Security Guard: Ensure only authorized roles/committees are here
    useEffect(() => {
        if (!user) return;

        if (!isSuperOrHighBoard && !isHRBoard && !isHRMember) {
            navigate('/', { replace: true });
        }
    }, [user, navigate, isSuperOrHighBoard, isHRBoard, isHRMember]);

    if (!user) return null;

    // Rows of the current list, filtered by the search box
    const listData = resources.data;
    const rows = Array.isArray(listData) ? listData : listData?.content ?? [];
    const lowerQ = searchQuery.toLowerCase();
    const filteredItems = searchQuery
        ? rows.filter((item) => (item.name || item.title || "").toLowerCase().includes(lowerQ))
        : rows;
    // Spring pages (events, clubs, programs) page; arrays (committees, exclusive forms) do not
    const pageInfo = listData && !Array.isArray(listData) ? listData : null;
    const resourceType = resourceTypeForTab(activeResourceTab);
    const activeSection = SHARED_RESOURCE_TABS.find((tab) => tab.id === activeResourceTab);

    const renderResourceContent = () => {
        if (activeResourceTab === 'users') {
            return <UserManagementTab hideManageAccess={HRMode} />;
        }

        return (
            <>
                {resources.error && (
                    <div role="alert" className={`${noticeClass.error} mb-4`}>
                        {resources.error}
                    </div>
                )}
                <ResourceTable
                    activeTab={activeResourceTab}
                    filteredItems={filteredItems}
                    loading={resources.loading}
                    isHRMode={HRMode}

                    // Attach the shared dialog handlers
                    onRegistrationClick={(item) => dialogs.openRegistration(item, resourceType)}
                    onQuestionsClick={(item) => dialogs.openQuestions(item, resourceType)}
                    onEmailsClick={(item) => dialogs.openEmails(item, resourceType)}
                    onEditMessageClick={dialogs.openCallMessage}

                    // Ignored in HR mode (No permissions)
                    onEditClick={() => { }}
                    onDeleteClick={() => { }}
                    onToggleCall={() => { }}
                />
                <ResourcePagination pageInfo={pageInfo} onPageChange={resources.setPage} />
            </>
        );
    };

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
                            tabs={SHARED_RESOURCE_TABS}
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

            <ResourceDialogs dialogs={dialogs} />
        </div>
    );
};

export default HRDashboardPage;
