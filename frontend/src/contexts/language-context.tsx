'use client';

import * as React from 'react';

type Language = 'en' | 'ru' | 'ky';

interface LanguageContextType {
  language: Language;
  setLanguage: (language: Language) => void;
  t: (key: string) => string;
}

const translations: Record<Language, Record<string, string>> = {
  en: {
    // Navigation
    'nav.dashboard': 'Dashboard',
    'nav.projects': 'Projects',
    'nav.tasks': 'My Tasks',
    'nav.teams': 'Teams',
    'nav.calendar': 'Calendar',
    'nav.time': 'Time Tracking',
    'nav.notifications': 'Notifications',
    'nav.settings': 'Settings',
    'nav.analytics': 'Analytics',
    'nav.admin': 'Admin Panel',
    'nav.logout': 'Logout',

    // Admin Navigation
    'nav.admin.title': 'Administration',
    'nav.admin.users': 'Users',
    'nav.admin.projects': 'All Projects',
    'nav.admin.tasks': 'All Tasks',
    'nav.admin.handbooks': 'Handbooks',
    'nav.admin.roles': 'System Roles',
    'nav.admin.permissions': 'Permissions',

    // Admin Page
    'admin.description': 'System administration and management',
    'admin.users.desc': 'Manage user accounts and roles',
    'admin.projects.desc': 'View and manage all projects',
    'admin.tasks.desc': 'View and manage all tasks',
    'admin.handbooks.desc': 'Manage system reference data',
    'admin.roles.desc': 'Manage system roles',
    'admin.permissions.desc': 'Configure API access permissions',

    // Settings page
    'settings.title': 'Settings',
    'settings.description': 'Manage your account settings and preferences',
    'settings.profile': 'Profile',
    'settings.notifications': 'Notifications',
    'settings.appearance': 'Appearance',
    'settings.security': 'Security',
    'settings.profileInfo': 'Profile Information',
    'settings.profileDesc': 'Update your personal information',
    'settings.firstName': 'First Name',
    'settings.lastName': 'Last Name',
    'settings.phone': 'Phone Number',
    'settings.saveChanges': 'Save Changes',
    'settings.notificationPrefs': 'Notification Preferences',
    'settings.notificationDesc': 'Choose what notifications you want to receive',
    'settings.emailNotifications': 'Email Notifications',
    'settings.emailNotificationsDesc': 'Receive notifications via email',
    'settings.notifyWhen': 'Notify me when:',
    'settings.taskAssigned': 'A task is assigned to me',
    'settings.taskCommented': 'Someone comments on my task',
    'settings.taskCompleted': 'A task I\'m watching is completed',
    'settings.deadlineReminder': 'Task deadline is approaching',
    'settings.appearanceTitle': 'Appearance',
    'settings.appearanceDesc': 'Customize how TaskFlow looks',
    'settings.theme': 'Theme',
    'settings.themeLight': 'Light',
    'settings.themeDark': 'Dark',
    'settings.themeSystem': 'System',
    'settings.themeDesc': 'Select your preferred theme',
    'settings.language': 'Language',
    'settings.languageDesc': 'Choose your preferred language',
    'settings.changePassword': 'Change Password',
    'settings.changePasswordDesc': 'Update your password to keep your account secure',
    'settings.currentPassword': 'Current Password',
    'settings.newPassword': 'New Password',
    'settings.confirmPassword': 'Confirm New Password',

    // Telegram
    'settings.telegramNotifications': 'Telegram Notifications',
    'settings.telegramNotificationsDesc': 'Receive notifications via Telegram',
    'settings.telegramConnected': 'Connected',
    'settings.telegramNotConnected': 'Not connected',
    'settings.telegramConnect': 'Connect Telegram',
    'settings.telegramDisconnect': 'Disconnect',
    'settings.telegramConnectTitle': 'Connect Telegram',
    'settings.telegramConnectDesc': 'Scan QR code or click the link to connect your Telegram account',
    'settings.telegramCode': 'Your code',
    'settings.telegramOpenBot': 'Open Telegram Bot',
    'settings.telegramConnectedAs': 'Connected as',
    'settings.inAppNotifications': 'In-App Notifications',
    'settings.inAppNotificationsDesc': 'Show notifications in the app',
    'settings.notificationChannels': 'Notification Channels',
    'settings.mentioned': 'Someone mentions me',
    'settings.projectInvite': 'I receive a project invitation',

    // Common
    'common.save': 'Save',
    'common.cancel': 'Cancel',
    'common.delete': 'Delete',
    'common.edit': 'Edit',
    'common.create': 'Create',
    'common.search': 'Search',
    'common.loading': 'Loading...',
    'common.error': 'Error',
    'common.success': 'Success',

    // Toast messages
    'toast.profileUpdated': 'Profile updated',
    'toast.profileUpdatedDesc': 'Your profile has been successfully updated.',
    'toast.profileError': 'Failed to update profile. Please try again.',
    'toast.passwordChanged': 'Password changed',
    'toast.passwordChangedDesc': 'Your password has been successfully changed.',
    'toast.passwordError': 'Failed to change password. Please check your current password.',

    // Calendar
    'calendar.title': 'Calendar',
    'calendar.description': 'View your tasks and deadlines',
    'calendar.noTasks': 'No tasks for this day',

    // Tasks
    'tasks.title': 'My Tasks',
    'tasks.description': 'View and manage tasks assigned to you',
    'tasks.search': 'Search tasks...',
    'tasks.filterAll': 'All',
    'tasks.noTasks': 'No tasks found',
    'tasks.noTasksDesc': 'You don\'t have any tasks assigned yet.',

    // AI Generator
    'nav.admin.aiGenerator': 'AI Generator',
    'admin.aiGenerator.desc': 'Generate forms and tables with AI',
    'ai.generator.title': 'AI Generator',
    'ai.generator.description': 'Generate forms and tables using AI based on natural language descriptions',
    'ai.generator.formTab': 'Form Generator',
    'ai.generator.tableTab': 'Table Generator',
    'ai.generator.formInput': 'Form Description',
    'ai.generator.formInputDesc': 'Describe what kind of form you want to generate',
    'ai.generator.tableInput': 'Table Description',
    'ai.generator.tableInputDesc': 'Describe what kind of table you want to generate',
    'ai.generator.prompt': 'Description',
    'ai.generator.formPromptPlaceholder': 'e.g., Create a user registration form with name, email, password, and phone number fields',
    'ai.generator.tablePromptPlaceholder': 'e.g., Create a table for managing products with name, price, quantity, and category columns',
    'ai.generator.entityType': 'Entity Type (optional)',
    'ai.generator.entityTypePlaceholder': 'e.g., User, Product, Order',
    'ai.generator.generating': 'Generating...',
    'ai.generator.generateForm': 'Generate Form',
    'ai.generator.generateTable': 'Generate Table',
    'ai.generator.preview': 'Preview',
    'ai.generator.noFormYet': 'Generated form will appear here',
    'ai.generator.noTableYet': 'Generated table will appear here',
  },
  ru: {
    // Navigation
    'nav.dashboard': 'Главная',
    'nav.projects': 'Проекты',
    'nav.tasks': 'Мои задачи',
    'nav.teams': 'Команды',
    'nav.calendar': 'Календарь',
    'nav.time': 'Учёт времени',
    'nav.notifications': 'Уведомления',
    'nav.settings': 'Настройки',
    'nav.analytics': 'Аналитика',
    'nav.admin': 'Админ панель',
    'nav.logout': 'Выйти',

    // Admin Navigation
    'nav.admin.title': 'Администрирование',
    'nav.admin.users': 'Пользователи',
    'nav.admin.projects': 'Все проекты',
    'nav.admin.tasks': 'Все задачи',
    'nav.admin.handbooks': 'Справочники',
    'nav.admin.roles': 'Роли системы',
    'nav.admin.permissions': 'Права доступа',

    // Admin Page
    'admin.description': 'Администрирование и управление системой',
    'admin.users.desc': 'Управление аккаунтами и ролями пользователей',
    'admin.projects.desc': 'Просмотр и управление всеми проектами',
    'admin.tasks.desc': 'Просмотр и управление всеми задачами',
    'admin.handbooks.desc': 'Управление справочными данными системы',
    'admin.roles.desc': 'Управление системными ролями',
    'admin.permissions.desc': 'Настройка прав доступа к API',

    // Settings page
    'settings.title': 'Настройки',
    'settings.description': 'Управление настройками аккаунта и предпочтениями',
    'settings.profile': 'Профиль',
    'settings.notifications': 'Уведомления',
    'settings.appearance': 'Внешний вид',
    'settings.security': 'Безопасность',
    'settings.profileInfo': 'Информация профиля',
    'settings.profileDesc': 'Обновите вашу личную информацию',
    'settings.firstName': 'Имя',
    'settings.lastName': 'Фамилия',
    'settings.phone': 'Номер телефона',
    'settings.saveChanges': 'Сохранить изменения',
    'settings.notificationPrefs': 'Настройки уведомлений',
    'settings.notificationDesc': 'Выберите какие уведомления вы хотите получать',
    'settings.emailNotifications': 'Email уведомления',
    'settings.emailNotificationsDesc': 'Получать уведомления по электронной почте',
    'settings.notifyWhen': 'Уведомлять когда:',
    'settings.taskAssigned': 'Мне назначена задача',
    'settings.taskCommented': 'Кто-то комментирует мою задачу',
    'settings.taskCompleted': 'Отслеживаемая задача выполнена',
    'settings.deadlineReminder': 'Приближается срок задачи',
    'settings.appearanceTitle': 'Внешний вид',
    'settings.appearanceDesc': 'Настройте внешний вид TaskFlow',
    'settings.theme': 'Тема',
    'settings.themeLight': 'Светлая',
    'settings.themeDark': 'Тёмная',
    'settings.themeSystem': 'Системная',
    'settings.themeDesc': 'Выберите предпочитаемую тему',
    'settings.language': 'Язык',
    'settings.languageDesc': 'Выберите предпочитаемый язык',
    'settings.changePassword': 'Изменить пароль',
    'settings.changePasswordDesc': 'Обновите пароль для безопасности аккаунта',
    'settings.currentPassword': 'Текущий пароль',
    'settings.newPassword': 'Новый пароль',
    'settings.confirmPassword': 'Подтвердите новый пароль',

    // Telegram
    'settings.telegramNotifications': 'Telegram уведомления',
    'settings.telegramNotificationsDesc': 'Получать уведомления через Telegram',
    'settings.telegramConnected': 'Подключен',
    'settings.telegramNotConnected': 'Не подключен',
    'settings.telegramConnect': 'Подключить Telegram',
    'settings.telegramDisconnect': 'Отключить',
    'settings.telegramConnectTitle': 'Подключение Telegram',
    'settings.telegramConnectDesc': 'Отсканируйте QR-код или нажмите на ссылку для подключения Telegram',
    'settings.telegramCode': 'Ваш код',
    'settings.telegramOpenBot': 'Открыть Telegram бот',
    'settings.telegramConnectedAs': 'Подключен как',
    'settings.inAppNotifications': 'Уведомления в приложении',
    'settings.inAppNotificationsDesc': 'Показывать уведомления в приложении',
    'settings.notificationChannels': 'Каналы уведомлений',
    'settings.mentioned': 'Кто-то упоминает меня',
    'settings.projectInvite': 'Получено приглашение в проект',

    // Common
    'common.save': 'Сохранить',
    'common.cancel': 'Отмена',
    'common.delete': 'Удалить',
    'common.edit': 'Редактировать',
    'common.create': 'Создать',
    'common.search': 'Поиск',
    'common.loading': 'Загрузка...',
    'common.error': 'Ошибка',
    'common.success': 'Успешно',

    // Toast messages
    'toast.profileUpdated': 'Профиль обновлён',
    'toast.profileUpdatedDesc': 'Ваш профиль успешно обновлён.',
    'toast.profileError': 'Не удалось обновить профиль. Попробуйте ещё раз.',
    'toast.passwordChanged': 'Пароль изменён',
    'toast.passwordChangedDesc': 'Ваш пароль успешно изменён.',
    'toast.passwordError': 'Не удалось изменить пароль. Проверьте текущий пароль.',

    // Calendar
    'calendar.title': 'Календарь',
    'calendar.description': 'Просмотр задач и сроков',
    'calendar.noTasks': 'Нет задач на этот день',

    // Tasks
    'tasks.title': 'Мои задачи',
    'tasks.description': 'Просмотр и управление назначенными задачами',
    'tasks.search': 'Поиск задач...',
    'tasks.filterAll': 'Все',
    'tasks.noTasks': 'Задачи не найдены',
    'tasks.noTasksDesc': 'Вам ещё не назначены задачи.',

    // AI Generator
    'nav.admin.aiGenerator': 'AI Генератор',
    'admin.aiGenerator.desc': 'Генерация форм и таблиц с помощью AI',
    'ai.generator.title': 'AI Генератор',
    'ai.generator.description': 'Генерация форм и таблиц с помощью AI на основе описания на естественном языке',
    'ai.generator.formTab': 'Генератор форм',
    'ai.generator.tableTab': 'Генератор таблиц',
    'ai.generator.formInput': 'Описание формы',
    'ai.generator.formInputDesc': 'Опишите какую форму вы хотите сгенерировать',
    'ai.generator.tableInput': 'Описание таблицы',
    'ai.generator.tableInputDesc': 'Опишите какую таблицу вы хотите сгенерировать',
    'ai.generator.prompt': 'Описание',
    'ai.generator.formPromptPlaceholder': 'например, Создай форму регистрации пользователя с полями имя, email, пароль и телефон',
    'ai.generator.tablePromptPlaceholder': 'например, Создай таблицу для управления товарами с колонками название, цена, количество и категория',
    'ai.generator.entityType': 'Тип сущности (необязательно)',
    'ai.generator.entityTypePlaceholder': 'например, Пользователь, Товар, Заказ',
    'ai.generator.generating': 'Генерация...',
    'ai.generator.generateForm': 'Сгенерировать форму',
    'ai.generator.generateTable': 'Сгенерировать таблицу',
    'ai.generator.preview': 'Предпросмотр',
    'ai.generator.noFormYet': 'Сгенерированная форма появится здесь',
    'ai.generator.noTableYet': 'Сгенерированная таблица появится здесь',
  },
  ky: {
    // Navigation
    'nav.dashboard': 'Башкы бет',
    'nav.projects': 'Долбоорлор',
    'nav.tasks': 'Менин тапшырмаларым',
    'nav.teams': 'Командалар',
    'nav.calendar': 'Календарь',
    'nav.time': 'Убакыт эсеби',
    'nav.notifications': 'Билдирүүлөр',
    'nav.settings': 'Жөндөөлөр',
    'nav.analytics': 'Аналитика',
    'nav.admin': 'Админ панели',
    'nav.logout': 'Чыгуу',

    // Admin Navigation
    'nav.admin.title': 'Администрация',
    'nav.admin.users': 'Колдонуучулар',
    'nav.admin.projects': 'Бардык долбоорлор',
    'nav.admin.tasks': 'Бардык тапшырмалар',
    'nav.admin.handbooks': 'Маалымат китеби',
    'nav.admin.roles': 'Система ролдору',
    'nav.admin.permissions': 'Уруксаттар',

    // Admin Page
    'admin.description': 'Системаны администрациялоо жана башкаруу',
    'admin.users.desc': 'Колдонуучу аккаунттарын жана ролдорун башкаруу',
    'admin.projects.desc': 'Бардык долбоорлорду көрүү жана башкаруу',
    'admin.tasks.desc': 'Бардык тапшырмаларды көрүү жана башкаруу',
    'admin.handbooks.desc': 'Системанын маалымат китептерин башкаруу',
    'admin.roles.desc': 'Система ролдорун башкаруу',
    'admin.permissions.desc': 'API уруксаттарын тууралоо',

    // Settings page
    'settings.title': 'Жөндөөлөр',
    'settings.description': 'Аккаунт жөндөөлөрүн башкаруу',
    'settings.profile': 'Профиль',
    'settings.notifications': 'Билдирүүлөр',
    'settings.appearance': 'Көрүнүш',
    'settings.security': 'Коопсуздук',
    'settings.profileInfo': 'Профиль маалыматы',
    'settings.profileDesc': 'Жеке маалыматыңызды жаңыртыңыз',
    'settings.firstName': 'Аты',
    'settings.lastName': 'Фамилиясы',
    'settings.phone': 'Телефон номери',
    'settings.saveChanges': 'Өзгөртүүлөрдү сактоо',
    'settings.notificationPrefs': 'Билдирүү жөндөөлөрү',
    'settings.notificationDesc': 'Кайсы билдирүүлөрдү алууну тандаңыз',
    'settings.emailNotifications': 'Email билдирүүлөр',
    'settings.emailNotificationsDesc': 'Email аркылуу билдирүүлөрдү алуу',
    'settings.notifyWhen': 'Мени кабарлоо:',
    'settings.taskAssigned': 'Мага тапшырма берилгенде',
    'settings.taskCommented': 'Тапшырмама комментарий жазылганда',
    'settings.taskCompleted': 'Көзөмөлдөгөн тапшырма аткарылганда',
    'settings.deadlineReminder': 'Тапшырманын мөөнөтү жакындаганда',
    'settings.appearanceTitle': 'Көрүнүш',
    'settings.appearanceDesc': 'TaskFlow көрүнүшүн тууралаңыз',
    'settings.theme': 'Тема',
    'settings.themeLight': 'Жарык',
    'settings.themeDark': 'Караңгы',
    'settings.themeSystem': 'Системалык',
    'settings.themeDesc': 'Каалаган теманы тандаңыз',
    'settings.language': 'Тил',
    'settings.languageDesc': 'Каалаган тилди тандаңыз',
    'settings.changePassword': 'Сырсөздү өзгөртүү',
    'settings.changePasswordDesc': 'Коопсуздук үчүн сырсөздү жаңыртыңыз',
    'settings.currentPassword': 'Учурдагы сырсөз',
    'settings.newPassword': 'Жаңы сырсөз',
    'settings.confirmPassword': 'Жаңы сырсөздү ырастаңыз',

    // Telegram
    'settings.telegramNotifications': 'Telegram билдирүүлөр',
    'settings.telegramNotificationsDesc': 'Telegram аркылуу билдирүүлөрдү алуу',
    'settings.telegramConnected': 'Туташкан',
    'settings.telegramNotConnected': 'Туташкан эмес',
    'settings.telegramConnect': 'Telegram туташтыруу',
    'settings.telegramDisconnect': 'Ажыратуу',
    'settings.telegramConnectTitle': 'Telegram туташтыруу',
    'settings.telegramConnectDesc': 'QR-кодду сканерлеңиз же шилтемени басыңыз',
    'settings.telegramCode': 'Сиздин код',
    'settings.telegramOpenBot': 'Telegram ботту ачуу',
    'settings.telegramConnectedAs': 'Туташкан',
    'settings.inAppNotifications': 'Колдонмодогу билдирүүлөр',
    'settings.inAppNotificationsDesc': 'Колдонмодо билдирүүлөрдү көрсөтүү',
    'settings.notificationChannels': 'Билдирүү каналдары',
    'settings.mentioned': 'Мени кимдир бирөө айтканда',
    'settings.projectInvite': 'Долбоорго чакыруу алганда',

    // Common
    'common.save': 'Сактоо',
    'common.cancel': 'Жокко чыгаруу',
    'common.delete': 'Өчүрүү',
    'common.edit': 'Түзөтүү',
    'common.create': 'Түзүү',
    'common.search': 'Издөө',
    'common.loading': 'Жүктөлүүдө...',
    'common.error': 'Ката',
    'common.success': 'Ийгилик',

    // Toast messages
    'toast.profileUpdated': 'Профиль жаңыртылды',
    'toast.profileUpdatedDesc': 'Профилиңиз ийгиликтүү жаңыртылды.',
    'toast.profileError': 'Профилди жаңыртуу мүмкүн болбоду. Кайра аракет кылыңыз.',
    'toast.passwordChanged': 'Сырсөз өзгөртүлдү',
    'toast.passwordChangedDesc': 'Сырсөзүңүз ийгиликтүү өзгөртүлдү.',
    'toast.passwordError': 'Сырсөздү өзгөртүү мүмкүн болбоду. Учурдагы сырсөздү текшериңиз.',

    // Calendar
    'calendar.title': 'Календарь',
    'calendar.description': 'Тапшырмаларды жана мөөнөттөрдү көрүү',
    'calendar.noTasks': 'Бул күнү тапшырма жок',

    // Tasks
    'tasks.title': 'Менин тапшырмаларым',
    'tasks.description': 'Дайындалган тапшырмаларды көрүү жана башкаруу',
    'tasks.search': 'Тапшырмаларды издөө...',
    'tasks.filterAll': 'Баары',
    'tasks.noTasks': 'Тапшырмалар табылган жок',
    'tasks.noTasksDesc': 'Сизге али тапшырма дайындалган эмес.',

    // AI Generator
    'nav.admin.aiGenerator': 'AI Генератор',
    'admin.aiGenerator.desc': 'AI менен форма жана таблицаларды түзүү',
    'ai.generator.title': 'AI Генератор',
    'ai.generator.description': 'Табигый тилде жазылган сүрөттөмө боюнча AI менен форма жана таблицаларды түзүү',
    'ai.generator.formTab': 'Форма генератору',
    'ai.generator.tableTab': 'Таблица генератору',
    'ai.generator.formInput': 'Форманын сүрөттөмөсү',
    'ai.generator.formInputDesc': 'Кандай форма түзүүнү каалаарыңызды сүрөттөңүз',
    'ai.generator.tableInput': 'Таблицанын сүрөттөмөсү',
    'ai.generator.tableInputDesc': 'Кандай таблица түзүүнү каалаарыңызды сүрөттөңүз',
    'ai.generator.prompt': 'Сүрөттөмө',
    'ai.generator.formPromptPlaceholder': 'мисалы, Колдонуучуну каттоо формасын түзүңүз: аты, email, сырсөз жана телефон талаалары менен',
    'ai.generator.tablePromptPlaceholder': 'мисалы, Товарларды башкаруу үчүн таблица түзүңүз: аталышы, баасы, саны жана категориясы мамычалары менен',
    'ai.generator.entityType': 'Объект түрү (милдеттүү эмес)',
    'ai.generator.entityTypePlaceholder': 'мисалы, Колдонуучу, Товар, Заказ',
    'ai.generator.generating': 'Түзүлүүдө...',
    'ai.generator.generateForm': 'Форма түзүү',
    'ai.generator.generateTable': 'Таблица түзүү',
    'ai.generator.preview': 'Алдын ала көрүү',
    'ai.generator.noFormYet': 'Түзүлгөн форма бул жерде көрүнөт',
    'ai.generator.noTableYet': 'Түзүлгөн таблица бул жерде көрүнөт',
  },
};

const LanguageContext = React.createContext<LanguageContextType | undefined>(undefined);

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguageState] = React.useState<Language>('ru');
  const [mounted, setMounted] = React.useState(false);

  React.useEffect(() => {
    const stored = localStorage.getItem('language') as Language | null;
    if (stored && ['en', 'ru', 'ky'].includes(stored)) {
      setLanguageState(stored);
    }
    setMounted(true);
  }, []);

  React.useEffect(() => {
    if (mounted) {
      document.documentElement.lang = language;
    }
  }, [language, mounted]);

  const setLanguage = React.useCallback((newLanguage: Language) => {
    setLanguageState(newLanguage);
    localStorage.setItem('language', newLanguage);
    document.documentElement.lang = newLanguage;
  }, []);

  const t = React.useCallback(
    (key: string): string => {
      return translations[language][key] || translations['en'][key] || key;
    },
    [language]
  );

  if (!mounted) {
    return <>{children}</>;
  }

  return (
    <LanguageContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useLanguage() {
  const context = React.useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
}
