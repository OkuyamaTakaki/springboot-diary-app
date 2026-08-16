/**
 * 日記アプリ共通JavaScript。
 * 通知モーダル、日記入力フォーム、日記編集機能を管理します。
 */

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

    messageElement.textContent = message;
    modal.style.display = "flex";
    modal.setAttribute("aria-hidden", "false");

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
    const text = document.getElementById("diaryFormToggleText");
    const icon = document.getElementById("diaryFormToggleIcon");

    if (!form || !text || !icon) {
        console.error("日記入力フォームのHTML要素が見つかりません。");
        return;
    }

    const isOpen = form.style.display === "block";

    if (isOpen) {
        form.style.display = "none";
        text.textContent = "日記を書く";
        icon.textContent = "▼";
    } else {
        form.style.display = "block";
        text.textContent = "日記を閉じる";
        icon.textContent = "▲";
    }
}

/**
 * ページ読み込み後にサーバーから渡された通知メッセージを確認します。
 */
document.addEventListener("DOMContentLoaded", function () {
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
            showNotification("日記を取得できませんでした。", "stay");
            return;
        }

        const diary = await response.json();

        titleInput.value = diary.title;
        contentInput.value = diary.content;
        editForm.dataset.id = diary.id;

        if (errorMessage) {
            errorMessage.textContent = "";
        }

        modal.style.display = "block";
        modal.setAttribute("aria-hidden", "false");
    } catch (error) {
        console.error("日記の取得中にエラーが発生しました。", error);
        showNotification("日記の取得中にエラーが発生しました。", "stay");
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
        const csrfToken = document.getElementById("csrfToken").value;

        const formData = new URLSearchParams();
        formData.append("title", title);
        formData.append("content", content);

        try {
            const response = await fetch(`/api/diary/${id}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "X-CSRF-TOKEN": csrfToken
                },
                body: formData
            });

            if (!response.ok) {
                const message = await response.text();
                showNotification(message, "stay");
                return;
            }

            closeEditModal();
            showNotification(`日記「${title}」を更新しました。`, "reload");
        } catch (error) {
            console.error("日記の更新中にエラーが発生しました。", error);
            showNotification("日記の更新中にエラーが発生しました。", "stay");
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
}