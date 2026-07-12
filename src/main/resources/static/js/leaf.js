let currentRecordId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const user = await checkAuthentication();
    if (!user) return;
});

// Tab Switching
const tabBtns = document.querySelectorAll('.tab-btn');
tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        tabBtns.forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(tc => tc.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById(btn.dataset.tab).classList.add('active');
        document.getElementById('resultSection').style.display = 'none';
        document.getElementById('errorAlert').style.display = 'none';
    });
});

// Image Upload Preview
const uploadZone = document.getElementById('uploadZone');
const imageFileInput = document.getElementById('imageFile');
const imagePreview = document.getElementById('imagePreview');

uploadZone.addEventListener('click', () => imageFileInput.click());

imageFileInput.addEventListener('change', () => {
    const file = imageFileInput.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            imagePreview.src = e.target.result;
            imagePreview.style.display = 'block';
        };
        reader.readAsDataURL(file);
    }
});

// Symptom Form Submission
const symptomForm = document.getElementById('symptomForm');
symptomForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    resetFormErrors();
    const errorAlert = document.getElementById('errorAlert');
    errorAlert.style.display = 'none';

    const payload = {
        cropName: document.getElementById('cropName').value.trim(),
        leafColor: document.getElementById('leafColor').value,
        leafCondition: document.getElementById('leafCondition').value,
        growth: document.getElementById('growth').value,
        temperature: parseFloat(document.getElementById('temperature').value),
        humidity: parseFloat(document.getElementById('humidity').value),
        rainfall: parseFloat(document.getElementById('rainfall').value),
        ph: parseFloat(document.getElementById('ph').value)
    };

    try {
        showLoading(true);
        const response = await fetch('/api/leaf/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        showLoading(false);

        const result = await response.json();
        if (response.ok) {
            currentRecordId = result.id;
            displaySymptomResult(result);
            showToast('Analysis complete!', 'success');
        } else {
            if (response.status === 400 && !result.error) {
                for (const key in result) {
                    displayError(`${key}Error`, result[key]);
                }
            } else {
                errorAlert.textContent = result.error || 'Failed to analyze symptoms.';
                errorAlert.style.display = 'block';
            }
        }
    } catch (err) {
        showLoading(false);
        errorAlert.textContent = 'Server connection lost. Try again later.';
        errorAlert.style.display = 'block';
    }
});

// Image Form Submission
const imageForm = document.getElementById('imageForm');
imageForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const errorAlert = document.getElementById('errorAlert');
    errorAlert.style.display = 'none';

    const file = imageFileInput.files[0];
    if (!file) {
        errorAlert.textContent = 'Please select an image to upload.';
        errorAlert.style.display = 'block';
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        showLoading(true);
        const response = await fetch('/api/leaf/upload', {
            method: 'POST',
            body: formData
        });
        showLoading(false);

        const result = await response.json();
        if (response.ok) {
            currentRecordId = result.id;
            displayImageResult(result);
            showToast('Analysis complete!', 'success');
        } else {
            errorAlert.textContent = result.error || 'Failed to analyze image.';
            errorAlert.style.display = 'block';
        }
    } catch (err) {
        showLoading(false);
        errorAlert.textContent = 'Server connection lost. Try again later.';
        errorAlert.style.display = 'block';
    }
});

function displaySymptomResult(data) {
    document.getElementById('resDisease').textContent = data.disease;
    document.getElementById('resConfidence').textContent = '';
    document.getElementById('resProblem').textContent = 'Primary Problem: ' + data.problem;
    document.getElementById('resNutrient').textContent = data.nutrientDeficiency;
    document.getElementById('resTreatment').textContent = data.recommendedFertilizer;
    document.getElementById('resDosage').textContent = data.dosage + ' (' + data.applicationMethod + ')';
    document.getElementById('resOrganic').textContent = data.organicSolution;
    document.getElementById('resPrecautions').textContent = data.precautions;
    showResult();
}

function displayImageResult(data) {
    document.getElementById('resDisease').textContent = data.disease;
    document.getElementById('resConfidence').textContent = data.confidence ? ('Confidence: ' + data.confidence) : '';
    document.getElementById('resProblem').textContent = '';
    document.getElementById('resNutrient').textContent = 'N/A';
    document.getElementById('resTreatment').textContent = data.treatment;
    document.getElementById('resDosage').textContent = data.recommendedFertilizer;
    document.getElementById('resOrganic').textContent = data.organicSolution;
    document.getElementById('resPrecautions').textContent = data.precautions;
    showResult();
}

function showResult() {
    const resultSection = document.getElementById('resultSection');
    resultSection.style.display = 'block';
    resultSection.scrollIntoView({ behavior: 'smooth' });
}

document.getElementById('downloadBtn').addEventListener('click', () => {
    if (!currentRecordId) return;
    window.location.href = `/api/history/download/${currentRecordId}?type=LEAF`;
});

document.getElementById('emailBtn').addEventListener('click', async () => {
    if (!currentRecordId) return;
    try {
        showLoading(true);
        const response = await fetch(`/api/history/email/${currentRecordId}?type=LEAF`, { method: 'POST' });
        showLoading(false);
        const result = await response.json();
        if (response.ok) {
            showToast(result.message || 'Report emailed successfully!', 'success');
        } else {
            showToast(result.error || 'Failed to email report.', 'danger');
        }
    } catch (err) {
        showLoading(false);
        showToast('Server connection failure', 'danger');
    }
});
