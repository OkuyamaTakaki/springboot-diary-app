/**
 * 日記アプリ共通JavaScript。
 * 通知モーダル、日記入力フォーム、日記編集機能を管理します。
 */

/**
 * サーバー側で翻訳済みの画面文言を取得します。
 * 設定要素がない認証画面でも安全に動作するよう、既定文言を受け取ります。
 *
 * @param {string} key data属性に対応するキー
 * @param {string} fallback 設定がない場合の文言
 * @returns {string} 表示用文言
 */
function getLocalizedMessage(key, fallback) {
    const messages = document.getElementById("localizedMessages");
    return messages?.dataset[key] || fallback;
}

let notificationReturnFocus = null;

/**
 * 言語切替後も検索日、並び順、ページ番号など現在の表示条件を維持します。
 * JavaScriptが無効な場合も、HTMLの相対リンクで言語切替自体は利用できます。
 */
function preserveViewStateInLanguageLinks() {
    document.querySelectorAll(".language-switch a[data-language]").forEach(function (link) {
        const targetUrl = new URL(window.location.href);
        targetUrl.searchParams.set("lang", link.dataset.language);
        link.href = `${targetUrl.pathname}${targetUrl.search}${targetUrl.hash}`;
    });
}

/**
 * 共通通知モーダルを表示します。
 *
 * @param {string} message 表示するメッセージ
 * @param {string} type OKボタン押下後の処理
 */
function showNotification(message, type = "reload") {
    const modal = document.getElementById("notificationModal");
    const messageElement = document.getElementById("notificationMessage");
    const okButton = document.getElementById("notificationOkButton");

    if (!modal || !messageElement || !okButton) {
        console.error("通知モーダルのHTML要素が見つかりません。");
        return;
    }

    notificationReturnFocus = document.activeElement instanceof HTMLElement
        ? document.activeElement
        : null;
    messageElement.textContent = message;
    modal.style.display = "flex";
    modal.setAttribute("aria-hidden", "false");
    okButton.focus();

    okButton.onclick = function () {
        modal.style.display = "none";
        modal.setAttribute("aria-hidden", "true");

        if (type === "login") {
            window.location.href = "/login";
            return;
        }

        if (type === "register") {
            window.location.href = "/register";
            return;
        }

        if (type === "stay") {
            notificationReturnFocus?.focus();
            notificationReturnFocus = null;
            return;
        }

        window.location.reload();
    };
}

/**
 * 日記入力フォームを開閉し、ボタンの表示を切り替えます。
 */
function toggleDiaryForm() {
    const form = document.getElementById("diaryWriteForm");

    if (!form) {
        console.error("日記入力フォームのHTML要素が見つかりません。");
        return;
    }

    const isOpen = form.style.display === "block";

    setDiaryFormOpen(!isOpen);
}

/**
 * 日記入力フォームの表示状態とアクセシビリティ属性を同期します。
 *
 * @param {boolean} isOpen フォームを開く場合はtrue
 */
function setDiaryFormOpen(isOpen) {
    const toggle = document.getElementById("diaryFormToggle");
    const form = document.getElementById("diaryWriteForm");
    const text = document.getElementById("diaryFormToggleText");
    const icon = document.getElementById("diaryFormToggleIcon");

    if (!form || !text || !icon) {
        console.error("日記入力フォームのHTML要素が見つかりません。");
        return;
    }

    if (isOpen) {
        form.style.display = "block";
        toggle?.setAttribute("aria-expanded", "true");
        text.textContent = getLocalizedMessage("closeWriter", "日記を閉じる");
        icon.textContent = "▲";
        return;
    }

    form.style.display = "none";
    toggle?.setAttribute("aria-expanded", "false");
    text.textContent = getLocalizedMessage("write", "日記を書く");
    icon.textContent = "▼";
}

/**
 * ページ読み込み後にサーバーから渡された通知メッセージを確認します。
 */
document.addEventListener("DOMContentLoaded", function () {
    const diaryFormToggle = document.getElementById("diaryFormToggle");
    const sortSelect = document.getElementById("sort");
    const editCancelButton = document.getElementById("editCancelButton");

    preserveViewStateInLanguageLinks();

    diaryFormToggle?.addEventListener("click", toggleDiaryForm);
    sortSelect?.addEventListener("change", function () {
        sortSelect.form?.requestSubmit();
    });
    editCancelButton?.addEventListener("click", closeEditModal);

    document.querySelectorAll(".btn-edit").forEach(function (button) {
        button.addEventListener("click", function () {
            openEditModal(button);
        });
    });

    const registerSuccessMessage = document.getElementById("registerSuccessMessage");
    const registerErrorMessage = document.getElementById("registerErrorMessage");
    const diaryErrorMessage = document.getElementById("diaryErrorMessage");
    const diarySuccessMessage = document.getElementById("diarySuccessMessage");
    const loginErrorMessage = document.getElementById("loginErrorMessage");

    if (registerSuccessMessage) {
        showNotification(registerSuccessMessage.textContent.trim(), "login");
        return;
    }

    if (registerErrorMessage) {
        showNotification(registerErrorMessage.textContent.trim(), "register");
        return;
    }

    if (loginErrorMessage) {
        showNotification(loginErrorMessage.textContent.trim(), "stay");
        return;
    }

    if (diaryErrorMessage) {
        setDiaryFormOpen(true);
        document.getElementById("content")?.focus();
        showNotification(diaryErrorMessage.textContent.trim(), "stay");
        return;
    }

    if (diarySuccessMessage) {
        showNotification(diarySuccessMessage.textContent.trim(), "reload");
    }
});

/**
 * 指定された日記を取得し、編集モーダルへ設定します。
 *
 * @param {HTMLElement} button 編集ボタン
 */
async function openEditModal(button) {
    const id = button.getAttribute("data-id");
    const modal = document.getElementById("editModal");
    const titleInput = document.getElementById("editTitle");
    const contentInput = document.getElementById("editContent");
    const editForm = document.getElementById("editForm");
    const errorMessage = document.getElementById("editErrorMessage");

    if (!modal || !titleInput || !contentInput || !editForm) {
        console.error("日記編集用のHTML要素が見つかりません。");
        return;
    }

    try {
        const response = await fetch(`/api/diary/${id}`);

        if (!response.ok) {
            showNotification(
                getLocalizedMessage("fetchError", "日記を取得できませんでした。"),
                "stay"
            );
            return;
        }

        const diary = await response.json();

        titleInput.value = diary.title;
        contentInput.value = diary.content;
        editForm.dataset.id = diary.id;

        if (errorMessage) {
            errorMessage.textContent = "";
        }

        modal.style.display = "flex";
        modal.setAttribute("aria-hidden", "false");
        modal.dataset.triggerId = id;
        titleInput.focus();
    } catch (error) {
        console.error("日記の取得中にエラーが発生しました。", error);
        showNotification(
            getLocalizedMessage("fetchUnexpected", "日記の取得中にエラーが発生しました。"),
            "stay"
        );
    }
}

/**
 * 日記編集フォームの非同期更新処理を設定します。
 */
document.addEventListener("DOMContentLoaded", function () {
    const editForm = document.getElementById("editForm");

    if (!editForm) {
        return;
    }

    editForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const id = editForm.dataset.id;
        const title = document.getElementById("editTitle").value;
        const content = document.getElementById("editContent").value;
        const csrfInput = document.getElementById("csrfToken");

        if (!csrfInput) {
            showNotification(
                getLocalizedMessage("csrfError", "セキュリティ情報を取得できませんでした。"),
                "stay"
            );
            return;
        }

        const formData = new URLSearchParams();
        formData.append("title", title);
        formData.append("content", content);
        formData.append(csrfInput.name, csrfInput.value);

        try {
            const response = await fetch(`/api/diary/${id}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: formData
            });

            if (!response.ok) {
                const message = await response.text();
                showNotification(message, "stay");
                return;
            }

            const message = await response.text();
            closeEditModal();
            showNotification(message, "reload");
        } catch (error) {
            console.error("日記の更新中にエラーが発生しました。", error);
            showNotification(
                getLocalizedMessage("updateUnexpected", "日記の更新中にエラーが発生しました。"),
                "stay"
            );
        }
    });
});

/**
 * 日記編集モーダルを閉じます。
 */
function closeEditModal() {
    const modal = document.getElementById("editModal");

    if (!modal) {
        return;
    }

    modal.style.display = "none";
    modal.setAttribute("aria-hidden", "true");

    const trigger = document.querySelector(
        `.btn-edit[data-id="${CSS.escape(modal.dataset.triggerId || "")}"]`
    );
    trigger?.focus();
    delete modal.dataset.triggerId;
}

document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
        closeEditModal();
    }
});
