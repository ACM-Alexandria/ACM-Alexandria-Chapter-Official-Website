import React, { useState, useEffect } from "react";
import Navbar from "../components/HomePage/Navbar";
import adminService, { fetchInsights } from "../services/adminService";
import {
  fetchHighBoard,
  fetchCommittee,
  fetchEvents,
  fetchClubs,
  fetchPrograms,
  fetchSeasons,
} from "../services/homePageService";
import SystemInsightsTab from "../components/AdminPage/InsightsSection/SystemInsightsTab";
import ManagementSidebar from "../components/AdminPage/ManagementSection/ManagementSidebar";
import ResourceTable from "../components/AdminPage/ManagementSection/ResourceTable";
import ResourceFormModal from "../components/AdminPage/ManagementSection/ResourceFormModal";
import DeleteConfirmModal from "../components/AdminPage/ManagementSection/DeleteConfirmModal";
import CallMessageModal from "../components/AdminPage/ManagementSection/CallMessageModal";
import RegistrationPanelModal from "../components/AdminPage/ManagementSection/RegistrationPanelModal";
import ClubSocialsModal from "../components/AdminPage/ManagementSection/ClubSocialsModal";
import QuestionsManagementModal from "../components/AdminPage/ManagementSection/QuestionsManagementModal";
import EpisodesManagementModal from "../components/AdminPage/ManagementSection/EpisodesManagementModal";
import EventGalleryModal from "../components/AdminPage/ManagementSection/EventGalleryModal";
import GalleryTab from "../components/AdminPage/ManagementSection/GalleryTab";
import FeedbackTab from "../components/AdminPage/ManagementSection/FeedbackTab";
import {
  FiUsers,
  FiCalendar,
  FiAward,
  FiBookOpen,
  FiUserCheck,
  FiLayers,
  FiCheckCircle,
  FiFileText,
  FiRefreshCw,
  FiLock,
  FiTrendingUp,
  FiSliders,
  FiActivity,
  FiAlertCircle,
  FiPlus,
  FiSearch,
  FiChevronLeft,
  FiChevronRight,
  FiAlertTriangle,
  FiShare2,
  FiHelpCircle,
  FiGrid,
  FiRadio,
  FiMessageSquare,
  FiGlobe,
} from "react-icons/fi";

/* ─── Brand ─── */
const B = "#4B98C8";
const BD = "#205E85";

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   ADMIN PAGE
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
const AdminPage = () => {
  const [insights, setInsights] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState("insights");

  // ── Resource Management States ──
  const [mgmtTab, setMgmtTab] = useState("highboard");
  const [mgmtSearchQuery, setMgmtSearchQuery] = useState("");
  const [mgmtLoading, setMgmtLoading] = useState(false);
  const [mgmtError, setMgmtError] = useState(null);

  // Data states
  const [highBoard, setHighBoard] = useState([]);
  const [committees, setCommittees] = useState([]);
  const [events, setEvents] = useState({ content: [], number: 0, totalPages: 1 });
  const [clubs, setClubs] = useState({ content: [], number: 0, totalPages: 1 });
  const [programs, setPrograms] = useState({ content: [], number: 0, totalPages: 1 });
  const [seasons, setSeasons] = useState({ content: [], number: 0, totalPages: 1 });
  const [exclusiveForms, setExclusiveForms] = useState([]);
  const [socialLinks, setSocialLinks] = useState([]);
  const [partners, setPartners] = useState([]);

  // Episodes management modal states
  const [episodesModalOpen, setEpisodesModalOpen] = useState(false);
  const [selectedSeasonForEpisodes, setSelectedSeasonForEpisodes] = useState(null);

  // Club Socials modal states
  const [socialsModalOpen, setSocialsModalOpen] = useState(false);
  const [selectedClubForSocials, setSelectedClubForSocials] = useState(null);

  // Form Questions modal states
  const [questionsModalOpen, setQuestionsModalOpen] = useState(false);
  const [selectedResourceForQuestions, setSelectedResourceForQuestions] = useState(null);
  const [questionsResourceType, setQuestionsResourceType] = useState("event");

  // Committee Board Member Specifics
  const [selectedCommitteeId, setSelectedCommitteeId] = useState("");

  // Modal / Form states
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState("add"); // 'add' or 'edit'
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({});

  // Delete Confirm Modal states
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deletingItem, setDeletingItem] = useState(null);

  // Message Modal states
  const [messageModalOpen, setMessageModalOpen] = useState(false);
  const [selectedCommittee, setSelectedCommittee] = useState(null);
  const [messageSubject, setMessageSubject] = useState("");
  const [messageBody, setMessageBody] = useState("");
  const [modalError, setModalError] = useState(null);

  // Registration Panel States
  const [regModalOpen, setRegModalOpen] = useState(false);
  const [selectedResourceForAnalysis, setSelectedResourceForAnalysis] = useState(null);
  const [regAnalysisData, setRegAnalysisData] = useState(null);
  const [regAnalysisLoading, setRegAnalysisLoading] = useState(false);
  const [regSyncLoading, setRegSyncLoading] = useState(false);
  const [regModalError, setRegModalError] = useState(null);

  // Event Gallery modal states
  const [galleryModalOpen, setGalleryModalOpen] = useState(false);
  const [selectedEventForGallery, setSelectedEventForGallery] = useState(null);

  const mgmtTabs = [
    { id: "highboard", label: "High Board", icon: FiUsers },
    { id: "committees", label: "Committees", icon: FiLayers },
    { id: "committeeBoard", label: "Committee Board", icon: FiUsers },
    { id: "events", label: "Events", icon: FiCalendar },
    { id: "clubs", label: "Clubs", icon: FiAward },
    { id: "programs", label: "Programs", icon: FiBookOpen },
    { id: "radio", label: "Radio", icon: FiRadio },
    { id: "exclusiveForms", label: "Exclusive Forms", icon: FiFileText },
    { id: "gallery", label: "Gallery", icon: FiGrid },
    { id: "socialLinks", label: "Social Links", icon: FiShare2 },
    { id: "partners", label: "Partners", icon: FiGlobe },
    { id: "feedback", label: "Grow Feedback", icon: FiMessageSquare },
  ];

  const loadMgmtTabData = async (tab, page = 0) => {
    setMgmtLoading(true);
    setMgmtError(null);
    try {
      if (tab === "highboard") {
        const data = await fetchHighBoard();
        setHighBoard(data.sort((a, b) => (a.order || 99) - (b.order || 99)));

      } else if (tab === "committees" || tab === "committeeBoard") {
        const data = await fetchCommittee();
        // Ensure boardRoles are sorted by order within each committee
        const sortedData = data.map((c) => ({
          ...c,
          boardRoles: (c.boardRoles || []).slice().sort((a, b) => (a.order ?? 99) - (b.order ?? 99)),
        }));
        setCommittees(sortedData);
        if (sortedData.length > 0 && !selectedCommitteeId) {
          setSelectedCommitteeId(sortedData[0].id.toString());
        }

      } else if (tab === "events") {
        const data = await fetchEvents(page);
        // Sort events by eventTime descending (latest first)
        const sorted = {
          ...data,
          content: data.content.sort((a, b) => new Date(b.eventTime) - new Date(a.eventTime)),
        };
        setEvents(sorted);
      } else if (tab === "clubs") {
        const data = await fetchClubs(page);
        // Sort clubs alphabetically by name
        const sorted = {
          ...data,
          content: data.content.sort((a, b) => a.name.localeCompare(b.name)),
        };
        setClubs(sorted);
      } else if (tab === "programs") {
        const data = await fetchPrograms(page);
        setPrograms(data);
      } else if (tab === "radio") {
        const data = await fetchSeasons(page);
        const sorted = {
          ...data,
          content: (data.content || []).sort((a, b) => b.seasonNumber - a.seasonNumber),
        };
        setSeasons(sorted);
      } else if (tab === "exclusiveForms") {
        const data = await adminService.fetchExclusiveForms();
        setExclusiveForms(data || []);
      } else if (tab === "socialLinks") {
        const data = await adminService.fetchSocialLinks();
        setSocialLinks(data);
      } else if (tab === "partners") {
        const data = await adminService.fetchPartners();
        setPartners(data || []);
      } else if (tab === "feedback") {
        // Handled internally in FeedbackTab
      }
    } catch (err) {
      console.error(err);
      setMgmtError("Failed to fetch resource data.");
    } finally {
      setMgmtLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === "management") {
      loadMgmtTabData(mgmtTab);
    }
  }, [activeTab, mgmtTab]);

  const getFilteredMgmtData = () => {
    const q = mgmtSearchQuery.toLowerCase();
    if (mgmtTab === "highboard") {
      return highBoard.filter(
        (m) =>
          m.name.toLowerCase().includes(q) ||
          m.role.toLowerCase().includes(q)
      );
    } else if (mgmtTab === "committees") {
      return committees.filter(
        (c) =>
          c.name.toLowerCase().includes(q) ||
          (c.description && c.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "committeeBoard") {
      const comm = committees.find((c) => c.id.toString() === selectedCommitteeId);
      if (!comm || !comm.boardRoles) return [];
      return comm.boardRoles.filter(
        (m) =>
          m.name.toLowerCase().includes(q) ||
          m.role.toLowerCase().includes(q)
      );
    } else if (mgmtTab === "events") {
      return events.content.filter(
        (e) =>
          e.name.toLowerCase().includes(q) ||
          (e.description && e.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "clubs") {
      return clubs.content.filter(
        (c) =>
          c.name.toLowerCase().includes(q) ||
          (c.description && c.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "programs") {
      return programs.content.filter(
        (p) =>
          p.name.toLowerCase().includes(q) ||
          (p.description && p.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "radio") {
      return seasons.content.filter(
        (s) =>
          s.seasonNumber.toString().includes(q)
      );
    } else if (mgmtTab === "exclusiveForms") {
      return exclusiveForms.filter(
        (f) =>
          (f.title && f.title.toLowerCase().includes(q)) ||
          (f.description && f.description.toLowerCase().includes(q))
      );
    } else if (mgmtTab === "socialLinks") {
      return socialLinks.filter(
        (sl) =>
          sl.platform.toLowerCase().includes(q) ||
          sl.url.toLowerCase().includes(q)
      );
    } else if (mgmtTab === "partners") {
      return partners.filter(
        (p) =>
          p.name.toLowerCase().includes(q) ||
          (p.description && p.description.toLowerCase().includes(q))
      );
    }
    return [];
  };

  const handleMgmtAddClick = () => {
    setFormMode("add");
    setEditingItem(null);
    setMgmtError(null);
    setModalError(null);

    if (mgmtTab === "highboard") {
      setFormData({ name: "", role: "", imageUrl: "", order: null, linkedinUrl: "" });
    } else if (mgmtTab === "committees") {
      setFormData({ name: "", description: "", logoUrl: "" });
    } else if (mgmtTab === "committeeBoard") {
      setFormData({ name: "", role: "", imageUrl: "", order: null, linkedinUrl: "" });
    } else if (mgmtTab === "events") {
      setFormData({ name: "", description: "", imageUrl: "", eventTime: "", location: "", attachedImages: [] });
    } else if (mgmtTab === "clubs") {
      setFormData({ name: "", description: "", imageUrl: "" });
    } else if (mgmtTab === "programs") {
      setFormData({ name: "", description: "", imageUrl: "", startDate: "", endDate: "", time: "", registrationOpen: false });
    } else if (mgmtTab === "radio") {
      setFormData({ seasonNumber: "", imageUrl: "" });
    } else if (mgmtTab === "exclusiveForms") {
      setFormData({ title: "", description: "", imageUrl: "", isActive: true });
    } else if (mgmtTab === "socialLinks") {
      setFormData({ platform: "", url: "" });
    } else if (mgmtTab === "partners") {
      setFormData({ name: "", website: "", imageUrl: "" });
    }
    setFormOpen(true);
  };

  const handleMgmtEditClick = (item) => {
    setFormMode("edit");
    setEditingItem(item);
    setMgmtError(null);
    setModalError(null);

    if (mgmtTab === "events" && item.eventTime) {
      const date = new Date(item.eventTime);
      const formattedDate = date.toISOString().slice(0, 16);
      setFormData({ ...item, eventTime: formattedDate });
    } else if (mgmtTab === "programs") {
      const formattedStartDate = item.startDate ? new Date(item.startDate).toISOString().slice(0, 16) : "";
      const formattedEndDate = item.endDate ? new Date(item.endDate).toISOString().slice(0, 16) : "";
      setFormData({ ...item, startDate: formattedStartDate, endDate: formattedEndDate });
    } else {
      setFormData({ ...item });
    }
    setFormOpen(true);
  };

  const handleMgmtFormSubmit = async (e) => {
    e.preventDefault();
    setMgmtLoading(true);
    setMgmtError(null);
    setModalError(null);
 
    try {
      if (mgmtTab === "highboard") {
        if (formMode === "add") {
          await adminService.addHighBoardMember(formData);
        } else {
          await adminService.updateHighBoardMember(editingItem.id, formData);
        }
      } else if (mgmtTab === "committees") {
        if (formMode === "add") {
          await adminService.createCommittee(formData);
        } else {
          await adminService.updateCommittee(editingItem.id, formData);
        }
      } else if (mgmtTab === "committeeBoard") {
        if (formMode === "add") {
          await adminService.addCommitteeBoardMember(parseInt(selectedCommitteeId), formData);
        } else {
          await adminService.updateCommitteeBoardMember(editingItem.id, formData);
        }
      } else if (mgmtTab === "events") {
        if (formMode === "add") {
          await adminService.createEvent(formData);
        } else {
          await adminService.updateEvent(editingItem.id, formData);
        }
      } else if (mgmtTab === "clubs") {
        if (formMode === "add") {
          await adminService.createClub(formData);
        } else {
          await adminService.updateClub(editingItem.id, formData);
        }
      } else if (mgmtTab === "programs") {
        if (formMode === "add") {
          await adminService.createProgram(formData);
        } else {
          await adminService.updateProgram(editingItem.id, formData);
        }
      } else if (mgmtTab === "radio") {
        if (formMode === "add") {
          await adminService.createSeason(formData);
        } else {
          await adminService.updateSeason(editingItem.id, formData);
        }
      } else if (mgmtTab === "exclusiveForms") {
        if (formMode === "add") {
          await adminService.createExclusiveForm(formData);
        } else {
          await adminService.updateExclusiveForm(editingItem.id, formData);
        }
      } else if (mgmtTab === "socialLinks") {
        if (formMode === "add") {
          await adminService.createSocialLink(formData);
        } else {
          await adminService.updateSocialLink(editingItem.id, formData);
        }
      } else if (mgmtTab === "partners") {
        if (formMode === "add") {
          await adminService.createPartner(formData);
        } else {
          await adminService.updatePartner(editingItem.id, formData);
        }
      }
 
      setFormOpen(false);
      loadMgmtTabData(mgmtTab);
    } catch (err) {
      console.error(err);
      const msg = err.message || err.error || (typeof err === "string" ? err : null) || `Failed to ${formMode} resource.`;
      setModalError(msg);
    } finally {
      setMgmtLoading(false);
    }
  };
 
  const handleMgmtDeleteClick = (item) => {
    setDeletingItem(item);
    setModalError(null);
    setDeleteOpen(true);
  };
 
  const handleConfirmMgmtDelete = async () => {
    setMgmtLoading(true);
    setMgmtError(null);
    setModalError(null);
    try {
      if (mgmtTab === "highboard") {
        await adminService.deleteHighBoardMember(deletingItem.id);
      } else if (mgmtTab === "committees") {
        await adminService.deleteCommittee(deletingItem.id);
      } else if (mgmtTab === "committeeBoard") {
        await adminService.deleteCommitteeBoardMember(deletingItem.id);
      } else if (mgmtTab === "events") {
        await adminService.deleteEvent(deletingItem.id);
      } else if (mgmtTab === "clubs") {
        await adminService.deleteClub(deletingItem.id);
      } else if (mgmtTab === "programs") {
        await adminService.deleteProgram(deletingItem.id);
      } else if (mgmtTab === "radio") {
        await adminService.deleteSeason(deletingItem.id);
      } else if (mgmtTab === "exclusiveForms") {
        await adminService.deleteExclusiveForm(deletingItem.id);
      } else if (mgmtTab === "socialLinks") {
        await adminService.deleteSocialLink(deletingItem.id);
      } else if (mgmtTab === "partners") {
        await adminService.deletePartner(deletingItem.id);
      }
 
      setDeleteOpen(false);
      setDeletingItem(null);
      loadMgmtTabData(mgmtTab);
    } catch (err) {
      console.error(err);
      const msg = err.message || err.error || (typeof err === "string" ? err : null) || "Failed to delete resource.";
      setModalError(msg);
    } finally {
      setMgmtLoading(false);
    }
  };

  const handleEpisodesClick = (season) => {
    setSelectedSeasonForEpisodes(season);
    setEpisodesModalOpen(true);
  };
 
  const handleToggleCall = async (item) => {
    setMgmtLoading(true);
    setMgmtError(null);
    try {
      if (mgmtTab === "programs") {
        const isCurrentlyOpen = item.registrationOpen;
        await adminService.toggleProgramRegistration(item.id, !isCurrentlyOpen);
      } else if (mgmtTab === "exclusiveForms") {
        const isCurrentlyActive = item.isActive;
        await adminService.updateExclusiveForm(item.id, {
          ...item,
          isActive: !isCurrentlyActive
        });
      } else {
        const isCurrentlyOpen = item.open || item.isOpen;
        if (isCurrentlyOpen) {
          await adminService.closeCommitteeCall(item.id);
        } else {
          await adminService.openCommitteeCall(item.id);
        }
      }
      await loadMgmtTabData(mgmtTab);
    } catch (err) {
      console.error(err);
      let errMsg = `Failed to update ${mgmtTab === "programs" ? "program registration" : mgmtTab === "exclusiveForms" ? "exclusive form status" : "committee call"} status.`;
      if (typeof err === "string") {
        errMsg = err;
      } else if (err && typeof err === "object") {
        errMsg = err.error || err.message || errMsg;
      }
      setMgmtError(errMsg);
    } finally {
      setMgmtLoading(false);
    }
  };
 
  const handleEditMessageClick = (committee) => {
    setSelectedCommittee(committee);
    setMessageSubject(committee.callMessage?.subject || "");
    setMessageBody(committee.callMessage?.body || "");
    setMgmtError(null);
    setModalError(null);
    setMessageModalOpen(true);
  };
 
  const handleMessageSubmit = async (e) => {
    e.preventDefault();
    setMgmtLoading(true);
    setMgmtError(null);
    setModalError(null);
    try {
      await adminService.changeCallMessage(selectedCommittee.id, {
        subject: messageSubject,
        body: messageBody,
      });
      setMessageModalOpen(false);
      await loadMgmtTabData(mgmtTab);
    } catch (err) {
      console.error(err);
      const msg = err.message || err.error || (typeof err === "string" ? err : null) || "Failed to update call message.";
      setModalError(msg);
    } finally {
      setMgmtLoading(false);
    }
  };

  const handleRegistrationClick = async (item) => {
    const resourceType = mgmtTab === "events" ? "event" : mgmtTab === "clubs" ? "club" : mgmtTab === "programs" ? "program" : mgmtTab === "exclusiveForms" ? "exclusive-form" : "committee";
    setSelectedResourceForAnalysis({
      id: item.id,
      name: item.name || item.title,
      type: resourceType,
    });
    setRegModalOpen(true);
    setRegAnalysisLoading(true);
    setRegModalError(null);
    setRegAnalysisData(null);
    
    if (resourceType !== "committee") {
      try {
        const data = await adminService.fetchRegistrationAnalysis(resourceType, item.id);
        setRegAnalysisData(data);
      } catch (err) {
        console.error(err);
        setRegModalError(err.message || err.error || "Failed to load registration analytics.");
      } finally {
        setRegAnalysisLoading(false);
      }
    } else {
      setRegAnalysisLoading(false);
    }
  };

  const handleQuestionsClick = (item) => {
    setSelectedResourceForQuestions(item);
    setQuestionsResourceType(
      mgmtTab === "events" ? "event" :
      mgmtTab === "clubs" ? "club" :
      mgmtTab === "programs" ? "program" :
      mgmtTab === "exclusiveForms" ? "exclusive-form" :
      "committee"
    );
    setQuestionsModalOpen(true);
  };

  const handleSocialsClick = (item) => {
    setSelectedClubForSocials(item);
    setSocialsModalOpen(true);
  };

  const handleGalleryClick = (item) => {
    setSelectedEventForGallery(item);
    setGalleryModalOpen(true);
  };

  const handleSyncRegistrationSheet = async () => {
    if (!selectedResourceForAnalysis) return;
    const { id, type } = selectedResourceForAnalysis;
    setRegSyncLoading(true);
    setRegModalError(null);
    try {
      const updatedData = await adminService.syncRegistrationSheet(type, id);
      setRegAnalysisData(updatedData);
      
      // Update local state list so URL and timestamp are updated in the main table data
      if (type === "event") {
        setEvents(prev => ({
          ...prev,
          content: prev.content.map(item => item.id === id ? { ...item, googleSheetUrl: updatedData.googleSheetUrl, sheetLastUpdatedAt: updatedData.sheetLastUpdatedAt } : item)
        }));
      } else if (type === "club") {
        setClubs(prev => ({
          ...prev,
          content: prev.content.map(item => item.id === id ? { ...item, googleSheetUrl: updatedData.googleSheetUrl, sheetLastUpdatedAt: updatedData.sheetLastUpdatedAt } : item)
        }));
      } else if (type === "program") {
        setPrograms(prev => ({
          ...prev,
          content: prev.content.map(item => item.id === id ? { ...item, googleSheetUrl: updatedData.googleSheetUrl, sheetLastUpdatedAt: updatedData.sheetLastUpdatedAt } : item)
        }));
      }
    } catch (err) {
      console.error(err);
      setRegModalError(err.message || err.error || "Failed to synchronize spreadsheet.");
    } finally {
      setRegSyncLoading(false);
    }
  };

  const loadInsights = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchInsights();
      setInsights(data);
    } catch (err) {
      console.error("Error loading admin insights:", err);
      setError(err.message || "Failed to load dashboard insights.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInsights();
  }, []);



  const tabs = [
    { id: "insights",   label: "System Insights",    icon: FiTrendingUp },
    { id: "management", label: "Resource Management", icon: FiSliders },
  ];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans">
      <Navbar activeSection="" />

      <main className="pt-[100px] pb-20 px-4 sm:px-6 md:px-10 lg:px-14 max-w-[1400px] mx-auto">
        {/* ── Header ── */}
        <div
          className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-8"
          style={{ animation: "fadeIn 0.5s ease both" }}
        >
          <div>
            <div className="flex items-center gap-2 mb-1">
              <FiActivity className="w-4 h-4" style={{ color: B }} />
              <span className="text-[10px] font-bold uppercase tracking-[0.2em]" style={{ color: B }}>
                Administration
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Dashboard
            </h1>
          </div>

          {activeTab === "insights" && (
            <button
              onClick={loadInsights}
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2.5 bg-white border border-slate-200 text-slate-600 hover:text-slate-900 hover:border-slate-300 rounded-xl shadow-sm text-xs font-bold tracking-wide uppercase active:scale-95 transition-all disabled:opacity-40"
            >
              <FiRefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`} style={{ color: B }} />
              Refresh
            </button>
          )}
        </div>

        {/* ── Tab Bar ── */}
        <div className="flex gap-1 bg-white border border-slate-200/80 p-1 rounded-xl w-fit shadow-sm mb-8">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-5 py-2.5 rounded-lg text-xs font-bold uppercase tracking-wider transition-all duration-200 ${
                activeTab === tab.id
                  ? "text-white shadow-md"
                  : "text-slate-500 hover:text-slate-700 hover:bg-slate-50"
              }`}
              style={activeTab === tab.id ? { background: `linear-gradient(135deg, ${B}, ${BD})` } : {}}
            >
              <tab.icon className="w-3.5 h-3.5" />
              {tab.label}
            </button>
          ))}
        </div>

        {/* ━━━━ TAB 1: INSIGHTS ━━━━ */}
        {activeTab === "insights" && (
          <SystemInsightsTab
            insights={insights}
            loading={loading}
            error={error}
            onRefresh={loadInsights}
          />
        )}

        {/* ━━━━ TAB 2: MANAGEMENT ━━━━ */}
        {activeTab === "management" && (
          <div className="flex flex-col lg:flex-row gap-8 items-start animate-[fadeIn_0.4s_ease]">
            <ManagementSidebar
              activeTab={mgmtTab}
              setActiveTab={setMgmtTab}
              tabs={mgmtTabs}
              setSearchQuery={setMgmtSearchQuery}
            />

            <div className="flex-1 w-full bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm min-h-[500px] flex flex-col justify-between">
              <div>
                {/* Header Controls — hidden for gallery/feedback which render their own headers */}
                {mgmtTab !== "gallery" && mgmtTab !== "feedback" && (
                  <>
                    <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
                      <div>
                        <h2 className="text-lg font-extrabold text-slate-800 tracking-tight capitalize">
                          Manage {mgmtTab === "highboard" ? "High Board" : mgmtTab === "committeeBoard" ? "Committee Board" : mgmtTab === "radio" ? "Radio Seasons" : mgmtTab === "partners" ? "Partners" : mgmtTab}
                        </h2>
                        <p className="text-xs text-slate-400 font-medium">
                          Add, edit, or delete items within this database category.
                        </p>
                      </div>

                      <button
                        onClick={handleMgmtAddClick}
                        className="flex items-center gap-2 px-4 py-2.5 bg-sky-500 hover:bg-sky-600 text-white rounded-xl text-xs font-bold uppercase tracking-wide shadow active:scale-95 transition-all"
                        style={{ backgroundColor: B }}
                      >
                        <FiPlus className="w-4 h-4" />
                        Add New
                      </button>
                    </div>

                    {/* Search Bar / Selector Row */}
                    <div className="flex flex-col sm:flex-row gap-4 mb-6">
                      <div className="relative flex-1">
                        <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                          <FiSearch className="w-4 h-4" />
                        </span>
                        <input
                          type="text"
                          value={mgmtSearchQuery}
                          onChange={(e) => setMgmtSearchQuery(e.target.value)}
                          placeholder="Search by name or description..."
                          className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 placeholder-slate-400 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                        />
                      </div>

                      {/* Committee Selector for Committee Board Management */}
                      {mgmtTab === "committeeBoard" && (
                        <select
                          value={selectedCommitteeId}
                          onChange={(e) => setSelectedCommitteeId(e.target.value)}
                          className="px-4 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-bold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                        >
                          {committees.map((c) => (
                            <option key={c.id} value={c.id}>
                              {c.name}
                            </option>
                          ))}
                        </select>
                      )}
                    </div>
                  </>
                )}

                {/* Error Alert */}
                {mgmtError && (
                  <div className="mb-6 bg-red-50 border border-red-200 text-red-600 rounded-xl p-4 flex items-center gap-3">
                    <FiAlertTriangle className="w-5 h-5 shrink-0" />
                    <p className="text-xs font-bold">{mgmtError}</p>
                  </div>
                )}

                {/* Table Data — skipped for gallery/feedback tabs which render their own UI */}
                {mgmtTab === "gallery" ? (
                  <GalleryTab />
                ) : mgmtTab === "feedback" ? (
                  <FeedbackTab />
                ) : (
                  <ResourceTable
                    activeTab={mgmtTab}
                    filteredItems={getFilteredMgmtData()}
                    loading={mgmtLoading}
                    onEditClick={handleMgmtEditClick}
                    onDeleteClick={handleMgmtDeleteClick}
                    onToggleCall={handleToggleCall}
                    onEditMessageClick={handleEditMessageClick}
                    onRegistrationClick={handleRegistrationClick}
                    onQuestionsClick={handleQuestionsClick}
                    onSocialsClick={handleSocialsClick}
                    onGalleryClick={handleGalleryClick}
                    onEpisodesClick={handleEpisodesClick}
                  />
                )}
              </div>

              {/* Pagination controls for paginated resources */}
              {(mgmtTab === "events" || mgmtTab === "clubs" || mgmtTab === "programs" || mgmtTab === "radio") && (
                <div className="flex items-center justify-between border-t border-slate-100 pt-5 mt-6">
                  <span className="text-xs text-slate-400 font-bold">
                    Page {(mgmtTab === "events" ? events.number : mgmtTab === "clubs" ? clubs.number : mgmtTab === "radio" ? seasons.number : programs.number) + 1} of{" "}
                    {mgmtTab === "events" ? events.totalPages : mgmtTab === "clubs" ? clubs.totalPages : mgmtTab === "radio" ? seasons.totalPages : programs.totalPages}
                  </span>
                  <div className="flex gap-2">
                    <button
                      disabled={mgmtTab === "events" ? events.number === 0 : mgmtTab === "clubs" ? clubs.number === 0 : mgmtTab === "radio" ? seasons.number === 0 : programs.number === 0}
                      onClick={() =>
                        loadMgmtTabData(mgmtTab, (mgmtTab === "events" ? events.number : mgmtTab === "clubs" ? clubs.number : mgmtTab === "radio" ? seasons.number : programs.number) - 1)
                      }
                      className="p-2 border border-slate-200 text-slate-500 hover:text-slate-800 disabled:opacity-40 rounded-lg active:scale-95 transition-all"
                    >
                      <FiChevronLeft className="w-4 h-4" />
                    </button>
                    <button
                      disabled={
                        mgmtTab === "events"
                          ? events.number >= events.totalPages - 1
                          : mgmtTab === "clubs"
                            ? clubs.number >= clubs.totalPages - 1
                            : mgmtTab === "radio"
                              ? seasons.number >= seasons.totalPages - 1
                              : programs.number >= programs.totalPages - 1
                      }
                      onClick={() =>
                        loadMgmtTabData(mgmtTab, (mgmtTab === "events" ? events.number : mgmtTab === "clubs" ? clubs.number : mgmtTab === "radio" ? seasons.number : programs.number) + 1)
                      }
                      className="p-2 border border-slate-200 text-slate-500 hover:text-slate-800 disabled:opacity-40 rounded-lg active:scale-95 transition-all"
                    >
                      <FiChevronRight className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              )}
            </div>

            {/* Modals */}
            <ResourceFormModal
              open={formOpen}
              onClose={() => { setFormOpen(false); setModalError(null); }}
              onSubmit={handleMgmtFormSubmit}
              formMode={formMode}
              activeTab={mgmtTab}
              formData={formData}
              setFormData={setFormData}
              loading={mgmtLoading}
              error={modalError}
            />

            <DeleteConfirmModal
              open={deleteOpen}
              onClose={() => { setDeleteOpen(false); setModalError(null); }}
              onConfirm={handleConfirmMgmtDelete}
              deletingItem={deletingItem}
              loading={mgmtLoading}
              error={modalError}
            />

            <CallMessageModal
              open={messageModalOpen}
              onClose={() => { setMessageModalOpen(false); setModalError(null); }}
              onSubmit={handleMessageSubmit}
              selectedCommittee={selectedCommittee}
              messageSubject={messageSubject}
              setMessageSubject={setMessageSubject}
              messageBody={messageBody}
              setMessageBody={setMessageBody}
              loading={mgmtLoading}
              error={modalError}
            />

            <RegistrationPanelModal
              open={regModalOpen}
              onClose={() => { setRegModalOpen(false); setRegModalError(null); }}
              resourceId={selectedResourceForAnalysis?.id}
              resourceName={selectedResourceForAnalysis?.name || ""}
              resourceType={selectedResourceForAnalysis?.type || ""}
              analysis={regAnalysisData}
              loading={regAnalysisLoading}
              syncLoading={regSyncLoading}
              onSyncSheet={handleSyncRegistrationSheet}
              error={regModalError}
            />

            <ClubSocialsModal
              open={socialsModalOpen}
              onClose={() => { setSocialsModalOpen(false); setModalError(null); }}
              club={selectedClubForSocials}
              onSaved={() => loadMgmtTabData(mgmtTab)}
            />

            <QuestionsManagementModal
              open={questionsModalOpen}
              onClose={() => { setQuestionsModalOpen(false); setModalError(null); }}
              resourceId={selectedResourceForQuestions?.id}
              resourceName={selectedResourceForQuestions?.name}
              resourceType={questionsResourceType}
            />

            <EpisodesManagementModal
              open={episodesModalOpen}
              onClose={() => { setEpisodesModalOpen(false); setModalError(null); }}
              seasonId={selectedSeasonForEpisodes?.id}
              seasonNumber={selectedSeasonForEpisodes?.seasonNumber}
            />

            <EventGalleryModal
              open={galleryModalOpen}
              onClose={() => { setGalleryModalOpen(false); setSelectedEventForGallery(null); }}
              event={selectedEventForGallery}
              onSave={() => loadMgmtTabData(mgmtTab)}
            />
          </div>
        )}
      </main>
    </div>
  );
};

export default AdminPage;
