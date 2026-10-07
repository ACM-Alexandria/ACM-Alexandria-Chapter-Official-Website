import React, { useState, useEffect, useRef } from "react";
import Navbar from "../components/HomePage/Navbar";
import adminService, { fetchInsights } from "../services/adminService";
import { useAuth } from "../contexts/AuthContext";
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
import CallMessageModal from "../components/AdminPage/ManagementSection/CallMessageModal";
import RegistrationPanelModal from "../components/AdminPage/ManagementSection/RegistrationPanelModal";
import ClubSocialsModal from "../components/AdminPage/ManagementSection/ClubSocialsModal";
import QuestionsManagementModal from "../components/AdminPage/ManagementSection/QuestionsManagementModal";
import EpisodesManagementModal from "../components/AdminPage/ManagementSection/EpisodesManagementModal";
import EventGalleryModal from "../components/AdminPage/ManagementSection/EventGalleryModal";
import RegistrationEmailsModal from "../components/AdminPage/ManagementSection/RegistrationEmailsModal";
import OpenCallConfirmModal from "../components/AdminPage/ManagementSection/OpenCallConfirmModal";
import { Highlight } from "../components/ThemedDialog";
import useThemedDialog from "../hooks/useThemedDialog";
import FeedbackTab from "../components/AdminPage/ManagementSection/FeedbackTab";
import HRFeedbackTab from "../components/AdminPage/ManagementSection/HRFeedbackTab";
import UserManagementTab from "../components/AdminPage/ManagementSection/UserManagementTab";
import GalleryTab from "../components/AdminPage/ManagementSection/GalleryTab";
import EmailCenterTab from "../components/AdminPage/EmailCenterSection/EmailCenterTab";
import {
  FiCalendar,
  FiAward,
  FiBookOpen,
  FiUserCheck,
  FiLayers,
  FiFileText,
  FiRefreshCw,
  FiTrendingUp,
  FiSliders,
  FiActivity,
  FiPlus,
  FiSearch,
  FiChevronLeft,
  FiChevronRight,
  FiAlertTriangle,
  FiShare2,
  FiGrid,
  FiRadio,
  FiMessageSquare,
  FiGlobe,
  FiMail,
  FiLock,
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
  const [mgmtTab, setMgmtTab] = useState("users");
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

  // Registration Emails modal states (events & clubs)
  const [emailsModalOpen, setEmailsModalOpen] = useState(false);
  const [selectedResourceForEmails, setSelectedResourceForEmails] = useState(null);

  // Committee open-call confirm states
  const [openCallCommittee, setOpenCallCommittee] = useState(null);

  // Double-click guards for email-sending actions (refs update immediately, unlike state)
  const announceInFlight = useRef(false);
  const openCallInFlight = useRef(false);
  const [announcingId, setAnnouncingId] = useState(null);

  // Site-wide email lock (only SUPER_ADMIN can change it)
  const { user } = useAuth();
  // Themed confirmations, errors and hints for the email actions (same look as the unsubscribe dialog)
  const { dialog, confirm, notify } = useThemedDialog();
  const isSuperAdmin = user?.role === "SUPER_ADMIN";
  const [emailsEnabled, setEmailsEnabled] = useState(true);
  const [emailLockSaving, setEmailLockSaving] = useState(false);

  const mgmtTabs = [
    { id: "users", label: "Users", icon: FiUserCheck },
    { id: "committees", label: "Committees", icon: FiLayers },
    { id: "events", label: "Events", icon: FiCalendar },
    { id: "clubs", label: "Clubs", icon: FiAward },
    { id: "programs", label: "Programs", icon: FiBookOpen },
    { id: "radio", label: "Radio", icon: FiRadio },
    { id: "exclusiveForms", label: "Exclusive Forms", icon: FiFileText },
    { id: "gallery", label: "Gallery", icon: FiGrid },
    { id: "socialLinks", label: "Social Links", icon: FiShare2 },
    { id: "partners", label: "Partners", icon: FiGlobe },
    { id: "feedback", label: "Grow Feedback", icon: FiMessageSquare },
    { id: "hrFeedback", label: "HR Feedback", icon: FiMessageSquare },
  ];

  const loadMgmtTabData = async (tab, page = 0) => {
    setMgmtLoading(true);
    setMgmtError(null);
    try {
      if (tab === "users") {
        // Users are fetched inside UserManagementTab
      } else if (tab === "highboard") {
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
      } else if (tab === "hrFeedback") {
        // Handled internally in HRFeedbackTab
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
          (m.name && m.name.toLowerCase().includes(q)) ||
          (m.role && m.role.toLowerCase().includes(q))
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
          (m.name && m.name.toLowerCase().includes(q)) ||
          (m.role && m.role.toLowerCase().includes(q))
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
      setFormData({ name: "", description: "", imageUrl: "", eventTime: "", endTime: "", location: "", attachedImages: [], registrationOpen: false, sendAnnouncement: true });
    } else if (mgmtTab === "clubs") {
      setFormData({ name: "", description: "", imageUrl: "", isExternal: false, registrationOpen: false, sendAnnouncement: true });
    } else if (mgmtTab === "programs") {
      setFormData({ name: "", description: "", imageUrl: "", startDate: "", endDate: "", time: "", registrationOpen: false, sendAnnouncement: true });
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
      const formattedEndDate = item.endTime
        ? new Date(item.endTime).toISOString().slice(0, 16)
        : "";

      setFormData({
        ...item,
        eventTime: formattedDate,
        endTime: formattedEndDate
      });
    } else if (mgmtTab === "programs") {
      const formattedStartDate = item.startDate ? new Date(item.startDate).toISOString().slice(0, 16) : "";
      const formattedEndDate = item.endDate ? new Date(item.endDate).toISOString().slice(0, 16) : "";
      setFormData({ ...item, startDate: formattedStartDate, endDate: formattedEndDate });
    } else if (mgmtTab === "committees") {
      // Only send fields relevant to the committee itself — boardRoles are managed separately
      const { boardRoles: _boardRoles, callMessage: _callMessage, topicToken: _topicToken, open: _open, ...committeeFields } = item;
      setFormData(committeeFields);
    } else if (mgmtTab === "highboard" || mgmtTab === "committeeBoard") {
      setFormData({
        ...item,
        name: item.user?.name || "",
        imageUrl: item.user?.profileImageUrl || "",
        linkedinUrl: item.user?.linkedinUrl || ""
      });
    } else {
      setFormData({ ...item });
    }
    setFormOpen(true);
  };

  const handleMgmtFormSubmit = async (e) => {
    e.preventDefault();

    // Inline Questions Validation
    if (mgmtTab === "events" && formMode === "add" && formData.questions) {
      for (const q of formData.questions) {
        if (!q.questionText || !q.questionText.trim()) {
          setModalError("Question text cannot be empty.");
          return;
        }
        if (q.questionType === "MULTIPLE_CHOICE" || q.questionType === "CHECKBOX") {
          const validOptions = (q.options || []).filter(opt => opt && opt.trim() !== "");
          if (validOptions.length === 0) {
            setModalError(`Question "${q.questionText}" must have at least one valid option.`);
            return;
          }
        }
      }
    }

    setMgmtLoading(true);
    setMgmtError(null);
    setModalError(null);
 
    try {
      let payload = { ...formData };
      if (mgmtTab === "highboard" && payload.user) {
        payload.user = {
          ...payload.user,
          name: payload.name !== undefined ? payload.name : payload.user.name,
          profileImageUrl: payload.imageUrl !== undefined ? payload.imageUrl : payload.user.profileImageUrl,
          linkedinUrl: payload.linkedinUrl !== undefined ? payload.linkedinUrl : payload.user.linkedinUrl
        };
      } else if (mgmtTab === "committeeBoard") {
        payload.userName = payload.name !== undefined ? payload.name : payload.userName;
        payload.profileImageUrl = payload.imageUrl !== undefined ? payload.imageUrl : payload.profileImageUrl;
        payload.linkedinUrl = payload.linkedinUrl !== undefined ? payload.linkedinUrl : payload.linkedinUrl;
      }

      if (mgmtTab === "highboard") {
        if (formMode === "add") {
          await adminService.addHighBoardMember(payload);
        } else {
          await adminService.updateHighBoardMember(editingItem.id, payload);
        }
      } else if (mgmtTab === "committees") {
        if (formMode === "add") {
          await adminService.createCommittee(formData);
        } else {
          await adminService.updateCommittee(editingItem.id, formData);
        }
      } else if (mgmtTab === "committeeBoard") {
        if (formMode === "add") {
          await adminService.addCommitteeBoardMember(parseInt(selectedCommitteeId), payload);
        } else {
          await adminService.updateCommitteeBoardMember(editingItem.id, payload);
        }
      } else if (mgmtTab === "events") {
        if (formMode === "add") {
          let newEvent = null;
          try {
            const { questions: _questions, ...eventPayload } = formData;
            newEvent = await adminService.createEvent(eventPayload);
            
            if (newEvent?.id && formData.questions && formData.questions.length > 0) {
              for (const q of formData.questions) {
                const payload = {
                  question_text: q.questionText,
                  question_type: q.questionType,
                  is_required: q.isRequired,
                  options: (q.questionType === "MULTIPLE_CHOICE" || q.questionType === "CHECKBOX") ? q.options : []
                };
                await adminService.createQuestion("event", newEvent.id, payload);
              }
            }
          } catch (error) {
            // Rollback the event if question creation failed
            if (newEvent?.id) {
              try {
                await adminService.deleteEvent(newEvent.id);
              } catch (rollbackError) {
                console.error("Failed to rollback event after question creation failure", rollbackError);
              }
            }
            throw error; // Let the outer catch handle the modal error state
          }

          setFormOpen(false);
          loadMgmtTabData(mgmtTab);
          return; // skip the generic setFormOpen(false) / loadMgmtTabData below
        } else {
          const { questions: _questions, ...eventPayload } = formData;
          await adminService.updateEvent(editingItem.id, eventPayload);
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
 
  const handleMgmtDeleteClick = async (item) => {
    if (deleteInFlight.current) return;
    deleteInFlight.current = true;

    try {
      const itemName = item.name || (item.seasonNumber !== undefined ? `Season ${item.seasonNumber}` : "this item");

      // Dynamic cascade-aware description based on entity type
      let title = "Delete Record?";
      let description = (
        <>
          Are you sure you want to delete <Highlight>{itemName}</Highlight>? This action is permanent and cannot be undone.
        </>
      );

      if (mgmtTab === "radio") {
        title = "Delete Season and All Episodes?";
        const episodeCount = item.episodesCount ?? item.episodes?.length;
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the season and{" "}
            {episodeCount !== undefined ? `all ${episodeCount} episodes` : "all episodes"}{" "}
            belonging to it. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "events") {
        title = "Delete Event?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the event, its custom questions, and all attendee registrations. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "clubs") {
        title = "Delete Club?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the club, its questions, and all club registrations. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "programs") {
        title = "Delete Program?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the program, its questions, and all participant registrations. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "committees") {
        title = "Delete Committee?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently remove the committee, its board members, questions, call history, and subscriber list. This action cannot be undone.
          </>
        );
      } else if (mgmtTab === "exclusiveForms") {
        title = "Delete Exclusive Form?";
        description = (
          <>
            Deleting <Highlight>{itemName}</Highlight> will permanently delete the form, all its configured questions, and submissions. This action cannot be undone.
          </>
        );
      }

      const confirmed = await confirm({
        tone: "danger",
        title,
        description,
        confirmLabel: "Yes, Delete",
        cancelLabel: "Cancel",
      });

      if (!confirmed) return;

      setMgmtLoading(true);
      setMgmtError(null);
      try {
        if (mgmtTab === "highboard") {
          await adminService.deleteHighBoardMember(item.id);
        } else if (mgmtTab === "committees") {
          await adminService.deleteCommittee(item.id);
        } else if (mgmtTab === "committeeBoard") {
          await adminService.deleteCommitteeBoardMember(item.id);
        } else if (mgmtTab === "events") {
          await adminService.deleteEvent(item.id);
        } else if (mgmtTab === "clubs") {
          await adminService.deleteClub(item.id);
        } else if (mgmtTab === "programs") {
          await adminService.deleteProgram(item.id);
        } else if (mgmtTab === "radio") {
          await adminService.deleteSeason(item.id);
        } else if (mgmtTab === "exclusiveForms") {
          await adminService.deleteExclusiveForm(item.id);
        } else if (mgmtTab === "socialLinks") {
          await adminService.deleteSocialLink(item.id);
        } else if (mgmtTab === "partners") {
          await adminService.deletePartner(item.id);
        }

        await loadMgmtTabData(mgmtTab);
      } catch (err) {
        console.error(err);
        const msg = err.message || err.error || (typeof err === "string" ? err : null) || "Failed to delete resource.";
        setMgmtError(msg);
      } finally {
        setMgmtLoading(false);
      }
    } finally {
      deleteInFlight.current = false;
    }
  };

  const handleEpisodesClick = (season) => {
    setSelectedSeasonForEpisodes(season);
    setEpisodesModalOpen(true);
  };
 
  const toggleInFlight = useRef(false);
  const syncSheetInFlight = useRef(false);
  const deleteInFlight = useRef(false);

  const handleToggleCall = async (item) => {
    if (toggleInFlight.current) return;
    toggleInFlight.current = true;

    try {
      const isCurrentlyOpen =
      mgmtTab === "programs"
        ? Boolean(item.registrationOpen)
        : mgmtTab === "exclusiveForms"
        ? Boolean(item.isActive)
        : Boolean(item.registrationOpen || item.open || item.isOpen);

    const resourceName = item.name || item.title || "this resource";

    // If opening a committee call, preserve OpenCallConfirmModal to configure emailing subscribers
    if (mgmtTab === "committees" && !isCurrentlyOpen) {
      setOpenCallCommittee(item);
      return;
    }

    // Determine confirmation tone, title, and description based on resource type and action direction
    const isClosing = isCurrentlyOpen;
    let title = "";
    let description = "";
    let tone = isClosing ? "warning" : "brand";
    let confirmLabel = isClosing ? "Close" : "Open";

    if (mgmtTab === "committees") {
      title = "Close Committee Call?";
      description = (
        <>
          Closing this call for <Highlight>{resourceName}</Highlight> will immediately stop accepting new committee applications. Existing applications will remain safely stored, but new applicants will no longer be able to submit until the call is reopened.
        </>
      );
      confirmLabel = "Close Call";
    } else if (mgmtTab === "exclusiveForms") {
      title = isClosing ? "Deactivate Form?" : "Activate Form?";
      description = isClosing ? (
        <>
          Deactivating <Highlight>{resourceName}</Highlight> will immediately prevent new submissions. Existing submissions will remain saved.
        </>
      ) : (
        <>
          Activating <Highlight>{resourceName}</Highlight> will make the form publicly available and allow new applicants to submit entries.
        </>
      );
      confirmLabel = isClosing ? "Deactivate" : "Activate";
    } else {
      title = isClosing ? "Close Registration?" : "Open Registration?";
      description = isClosing ? (
        <>
          Closing registration for <Highlight>{resourceName}</Highlight> will immediately stop accepting new registrations. Existing registrations will remain preserved.
        </>
      ) : (
        <>
          Opening registration for <Highlight>{resourceName}</Highlight> will make the registration form publicly available and allow new applicants to register.
        </>
      );
      confirmLabel = isClosing ? "Close Registration" : "Open Registration";
    }

    const confirmed = await confirm({
      tone,
      title,
      description,
      confirmLabel,
      cancelLabel: "Cancel",
    });

    if (!confirmed) return;

    setMgmtLoading(true);
    setMgmtError(null);
    try {
      if (mgmtTab === "programs") {
        await adminService.toggleProgramRegistration(item.id, !isCurrentlyOpen);
      } else if (mgmtTab === "clubs") {
        if (isCurrentlyOpen) {
          await adminService.closeClubCall(item.id);
        } else {
          await adminService.openClubCall(item.id);
        }
      } else if (mgmtTab === "events") {
        if (isCurrentlyOpen) {
          await adminService.closeEventCall(item.id);
        } else {
          await adminService.openEventCall(item.id);
        }
      } else if (mgmtTab === "exclusiveForms") {
        await adminService.updateExclusiveForm(item.id, {
          ...item,
          isActive: !isCurrentlyOpen,
        });
      } else {
        await adminService.closeCommitteeCall(item.id);
      }
      await loadMgmtTabData(mgmtTab);
    } catch (err) {
      console.error(err);
      let errMsg = `Failed to update ${
        mgmtTab === "programs"
          ? "program registration"
          : mgmtTab === "exclusiveForms"
          ? "exclusive form status"
          : "committee call"
      } status.`;
      if (typeof err === "string") {
        errMsg = err;
      } else if (err && typeof err === "object") {
        errMsg = err.error || err.message || errMsg;
      }
      setMgmtError(errMsg);
    } finally {
      setMgmtLoading(false);
    }
  } finally {
    toggleInFlight.current = false;
  }
};

  const handleConfirmOpenCall = async (sendAnnouncement) => {
    if (openCallInFlight.current) return;
    openCallInFlight.current = true;
    setMgmtLoading(true);
    setMgmtError(null);
    try {
      await adminService.openCommitteeCall(openCallCommittee.id, sendAnnouncement);
      setOpenCallCommittee(null);
      await loadMgmtTabData(mgmtTab);
    } catch (err) {
      console.error(err);
      setOpenCallCommittee(null);
      setMgmtError(err?.error || err?.message || (typeof err === "string" ? err : "Failed to open committee call."));
    } finally {
      openCallInFlight.current = false;
      setMgmtLoading(false);
    }
  };

  const handleAnnounceClick = async (item) => {
    // Claimed before the confirm prompt so a double-click can't open a second prompt or send twice
    if (announceInFlight.current) return;
    announceInFlight.current = true;
    const isCommittee = mgmtTab === "committees";
    const ok = await confirm({
      tone: "brand",
      title: isCommittee ? "Resend Call Email?" : "Send Announcement?",
      message: isCommittee
        ? <>Resend the <Highlight>{item.name}</Highlight> call email to its subscribers?</>
        : <>Send the <Highlight>{item.name}</Highlight> announcement email to all newsletter subscribers?</>,
      confirmLabel: "Yes, Send",
    });
    if (!ok) {
      announceInFlight.current = false;
      return;
    }

    setAnnouncingId(item.id);
    setMgmtLoading(true);
    setMgmtError(null);
    try {
      const sent = await adminService.sendWithResendPrompt(
        (force) =>
          isCommittee
            ? adminService.announceCommitteeCall(item.id, force)
            : adminService.sendAnnouncement(mgmtTab, item.id, force),
        confirm
      );
      if (sent) {
        notify({ tone: "success", title: "Emails On The Way", message: "The emails are being sent to subscribers." });
      }
    } catch (err) {
      console.error(err);
      notify({
        tone: "danger",
        title: "Couldn't Send",
        message: err?.error || err?.message || (typeof err === "string" ? err : "Failed to send announcement."),
      });
    } finally {
      announceInFlight.current = false;
      setAnnouncingId(null);
      setMgmtLoading(false);
    }
  };

  const handleEmailsClick = (item) => {
    setSelectedResourceForEmails({ item, type: mgmtTab });
    setEmailsModalOpen(true);
  };

  useEffect(() => {
    adminService.fetchEmailLock()
      .then((data) => setEmailsEnabled(data?.emailsEnabled !== false))
      .catch((err) => console.error("Failed to load email lock status", err));
  }, []);

  const emailLockInFlight = useRef(false);

  const handleToggleEmailLock = async () => {
    if (emailLockInFlight.current || emailLockSaving) return;
    emailLockInFlight.current = true;

    try {
      const enable = !emailsEnabled;
      if (!enable) {
        const ok = await confirm({
          tone: "danger",
          title: "Lock All Emails?",
          description: "No emails or notifications will be sent (except password resets) until you unlock them.",
          confirmLabel: "Yes, Lock",
          cancelLabel: "Cancel",
        });
        if (!ok) return;
      } else {
        const ok = await confirm({
          tone: "brand",
          title: "Enable System Emails?",
          description: "This will re-enable system-wide email delivery for notifications and other email operations.",
          confirmLabel: "Yes, Enable",
          cancelLabel: "Cancel",
        });
        if (!ok) return;
      }
      setEmailLockSaving(true);
      try {
        const data = await adminService.updateEmailLock(enable);
        setEmailsEnabled(data?.emailsEnabled !== false);
      } catch (err) {
        console.error(err);
        notify({ tone: "danger", title: "Couldn't Update", message: err?.message || err?.error || "Failed to update the email lock." });
      } finally {
        setEmailLockSaving(false);
      }
    } finally {
      emailLockInFlight.current = false;
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
    if (syncSheetInFlight.current) return;
    syncSheetInFlight.current = true;

    try {
      const resourceName = selectedResourceForAnalysis.name || "this resource";

      const confirmed = await confirm({
        tone: "danger",
        title: "Overwrite Google Sheet?",
        description: (
          <>
            This will replace the existing spreadsheet contents for <Highlight>{resourceName}</Highlight> with the latest registration data from the website. Any manual notes, formulas, or other changes currently in the Google Sheet may be permanently erased.
          </>
        ),
        confirmLabel: "Yes, Overwrite Sheet",
        cancelLabel: "Cancel",
      });

      if (!confirmed) return;

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
    } finally {
      syncSheetInFlight.current = false;
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
    { id: "insights", label: "System Insights", icon: FiTrendingUp },
    { id: "management", label: "Resource Management", icon: FiSliders },
    { id: "email", label: "Email Center", icon: FiMail },
  ];

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-slate-100 font-sans">
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
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 dark:text-slate-100 tracking-tight">
              Dashboard
            </h1>
          </div>

          <div className="flex items-center gap-3">
          {isSuperAdmin && (
            <button
              type="button"
              onClick={handleToggleEmailLock}
              disabled={emailLockSaving}
              title={emailsEnabled ? "Lock all site emails" : "Unlock site emails"}
              className="flex items-center gap-2.5 px-4 py-2.5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl shadow-sm dark:shadow-slate-950/40 text-xs font-bold tracking-wide uppercase text-slate-600 dark:text-slate-200 active:scale-95 transition-all disabled:opacity-40"
            >
              <FiLock className="w-3.5 h-3.5" style={{ color: emailsEnabled ? B : "#dc2626" }} />
              Site Emails
              <span
                className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors duration-300 ${
                  emailsEnabled ? "bg-[#4B98C8]" : "bg-red-500"
                }`}
              >
                <span
                  className={`inline-block h-3.5 w-3.5 transform rounded-full bg-white shadow-md transition-transform duration-300 ${
                    emailsEnabled ? "translate-x-4" : "translate-x-1"
                  }`}
                />
              </span>
            </button>
          )}
          {activeTab === "insights" && (
            <button
              onClick={loadInsights}
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2.5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-200 hover:text-slate-900 dark:hover:text-slate-100 hover:border-slate-300 dark:hover:border-slate-600 rounded-xl shadow-sm dark:shadow-slate-950/40 text-xs font-bold tracking-wide uppercase active:scale-95 transition-all disabled:opacity-40"
            >
              <FiRefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`} style={{ color: B }} />
              Refresh
            </button>
          )}
          </div>
        </div>

        {/* ── Email lock warning ── */}
        {!emailsEnabled && (
          <div className="flex items-center gap-3 p-4 mb-8 bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-200 rounded-xl">
            <FiAlertTriangle className="w-5 h-5 shrink-0" />
            <p className="text-xs font-bold">
              All site emails are locked. No emails or notifications are sent (except password resets).
              {isSuperAdmin ? " Use the Site Emails switch above to unlock." : " Only the super admin can unlock them."}
            </p>
          </div>
        )}

        {/* ── Tab Bar ── */}
        <div className="flex gap-1 bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-700 p-1 rounded-xl w-fit shadow-sm dark:shadow-slate-950/40 mb-8">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-5 py-2.5 rounded-lg text-xs font-bold uppercase tracking-wider transition-all duration-200 ${
                activeTab === tab.id
                  ? "text-white shadow-md"
                  : "text-slate-500 dark:text-slate-300 hover:text-slate-700 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-800"
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

        {activeTab === "email" && (
          <EmailCenterTab />
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

            <div className="flex-1 w-full bg-white dark:bg-slate-900 rounded-2xl border border-slate-200/80 dark:border-slate-700 p-6 shadow-sm dark:shadow-slate-950/40 min-h-[500px] flex flex-col justify-between">
              <div>
                {/* Header Controls — hidden for gallery/feedback/hrFeedback/users which render their own headers */}
                {mgmtTab !== "gallery" && mgmtTab !== "feedback" && mgmtTab !== "hrFeedback" && mgmtTab !== "users" && (
                  <>
                    <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
                      <div>
                        <h2 className="text-lg font-extrabold text-slate-800 dark:text-slate-100 tracking-tight capitalize">
                          Manage {mgmtTab === "highboard" ? "High Board" : mgmtTab === "committeeBoard" ? "Committee Board" : mgmtTab === "radio" ? "Radio Seasons" : mgmtTab === "partners" ? "Partners" : mgmtTab}
                        </h2>
                        <p className="text-xs text-slate-400 dark:text-slate-300 font-medium">
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
                          className="w-full pl-10 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 placeholder-slate-400 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                        />
                      </div>

                      {/* Committee Selector for Committee Board Management */}
                      {mgmtTab === "committeeBoard" && (
                        <select
                          value={selectedCommitteeId}
                          onChange={(e) => setSelectedCommitteeId(e.target.value)}
                          className="px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 text-xs font-bold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
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
                  <div className="mb-6 bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-red-600 dark:text-red-200 rounded-xl p-4 flex items-center gap-3">
                    <FiAlertTriangle className="w-5 h-5 shrink-0" />
                    <p className="text-xs font-bold">{mgmtError}</p>
                  </div>
                )}

                {/* Table Data — skipped for gallery/feedback/hrFeedback/users tabs which render their own UI */}
                {mgmtTab === "users" ? (
                  <UserManagementTab />
                ) : mgmtTab === "gallery" ? (
                  <GalleryTab />
                ) : mgmtTab === "feedback" ? (
                  <FeedbackTab />
                ) : mgmtTab === "hrFeedback" ? (
                  <HRFeedbackTab />
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
                    onAnnounceClick={handleAnnounceClick}
                    announcingId={announcingId}
                    onEmailsClick={handleEmailsClick}
                  />
                )}
              </div>

              {/* Pagination controls for paginated resources */}
              {(mgmtTab === "events" || mgmtTab === "clubs" || mgmtTab === "programs" || mgmtTab === "radio") && (
                <div className="flex items-center justify-between border-t border-slate-100 dark:border-slate-700 pt-5 mt-6">
                  <span className="text-xs text-slate-400 dark:text-slate-300 font-bold">
                    Page {(mgmtTab === "events" ? events.number : mgmtTab === "clubs" ? clubs.number : mgmtTab === "radio" ? seasons.number : programs.number) + 1} of{" "}
                    {mgmtTab === "events" ? events.totalPages : mgmtTab === "clubs" ? clubs.totalPages : mgmtTab === "radio" ? seasons.totalPages : programs.totalPages}
                  </span>
                  <div className="flex gap-2">
                    <button
                      disabled={mgmtTab === "events" ? events.number === 0 : mgmtTab === "clubs" ? clubs.number === 0 : mgmtTab === "radio" ? seasons.number === 0 : programs.number === 0}
                      onClick={() =>
                        loadMgmtTabData(mgmtTab, (mgmtTab === "events" ? events.number : mgmtTab === "clubs" ? clubs.number : mgmtTab === "radio" ? seasons.number : programs.number) - 1)
                      }
                      className="p-2 border border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100 disabled:opacity-40 rounded-lg active:scale-95 transition-all"
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
                      className="p-2 border border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100 disabled:opacity-40 rounded-lg active:scale-95 transition-all"
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

            <RegistrationEmailsModal
              open={emailsModalOpen}
              onClose={() => { setEmailsModalOpen(false); setSelectedResourceForEmails(null); }}
              resource={selectedResourceForEmails?.item}
              resourceType={selectedResourceForEmails?.type}
            />

            <OpenCallConfirmModal
              open={Boolean(openCallCommittee)}
              onClose={() => setOpenCallCommittee(null)}
              onConfirm={handleConfirmOpenCall}
              committee={openCallCommittee}
              loading={mgmtLoading}
            />
          </div>
        )}
      </main>
      {dialog}
    </div>
  );
};

export default AdminPage;
