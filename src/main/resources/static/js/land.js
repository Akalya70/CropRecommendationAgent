let currentRecordId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const user = await checkAuthentication();
    if (!user) return;
});

const landForm = document.getElementById('landForm');
landForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    resetFormErrors();
    const errorAlert = document.getElementById('errorAlert');
    errorAlert.style.display = 'none';

    const payload = {
        nitrogen: parseFloat(document.getElementById('nitrogen').value),
        phosphorus: parseFloat(document.getElementById('phosphorus').value),
        potassium: parseFloat(document.getElementById('potassium').value),
        temperature: parseFloat(document.getElementById('temperature').value),
        humidity: parseFloat(document.getElementById('humidity').value),
        ph: parseFloat(document.getElementById('ph').value),
        rainfall: parseFloat(document.getElementById('rainfall').value),
        state: document.getElementById('state').value.trim(),
        district: document.getElementById('district').value.trim()
    };

    try {
        showLoading(true);
        const response = await fetch('/api/land/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        showLoading(false);

        const result = await response.json();
        if (response.ok) {
            currentRecordId = result.id;
            displayResult(result);
            showToast('Analysis complete!', 'success');
        } else {
            if (response.status === 400 && !result.error) {
                for (const key in result) {
                    displayError(`${key}Error`, result[key]);
                }
            } else {
                errorAlert.textContent = result.error || 'Failed to analyze land conditions.';
                errorAlert.style.display = 'block';
            }
        }
    } catch (err) {
        showLoading(false);
        errorAlert.textContent = 'Server connection lost. Try again later.';
        errorAlert.style.display = 'block';
    }
});

function displayResult(data) {
    document.getElementById('resCropName').textContent = data.cropName;
    document.getElementById('resReason').textContent = data.reason;
    document.getElementById('resSeason').textContent = data.suitableSeason;
    document.getElementById('resWater').textContent = data.waterRequirement;
    document.getElementById('resYield').textContent = data.expectedYield;
    document.getElementById('resTips').textContent = data.cultivationTips;

    const resultSection = document.getElementById('resultSection');
    resultSection.style.display = 'block';
    resultSection.scrollIntoView({ behavior: 'smooth' });
}

document.getElementById('downloadBtn').addEventListener('click', () => {
    if (!currentRecordId) return;
    window.location.href = `/api/history/download/${currentRecordId}?type=LAND`;
});

document.getElementById('emailBtn').addEventListener('click', async () => {
    if (!currentRecordId) return;
    try {
        showLoading(true);
        const response = await fetch(`/api/history/email/${currentRecordId}?type=LAND`, { method: 'POST' });
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
