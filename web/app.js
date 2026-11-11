'use strict';
const $ = selector => document.querySelector(selector);
const names = { book: 'Book', candle: 'Candle', tea: 'Tea collection' };
const money = amount => new Intl.NumberFormat('en-US').format(amount) + ' ₸';
let items = [];
let nextId = 1;
let revision = 0;
let currentQuote = null;
let receipt = null;
let placing = false;
let quoteTimer;

function element(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

function renderItems() {
  const container = $('#items');
  container.replaceChildren();
  if (!items.length) {
    const empty = element('div', 'empty-state');
    empty.append(element('span', '', '✧'), element('p', '', 'Your box starts here.'),
      element('small', '', 'Add something above, then choose its wrapping.'));
    container.append(empty);
  }
  items.forEach(item => {
    const row = element('div', 'item');
    row.append(element('span', 'item-name', names[item.product]));
    const remove = element('button', 'remove', 'Remove');
    remove.type = 'button';
    remove.setAttribute('aria-label', `Remove ${names[item.product]} ${item.id}`);
    remove.addEventListener('click', () => { items = items.filter(other => other.id !== item.id); renderItems(); scheduleQuote(); });
    row.append(remove);
    const options = element('div', 'item-options');
    [['wrapped', 'Gift wrap +200 ₸'], ['ribbon', 'Ribbon +100 ₸']].forEach(([key, title]) => {
      const label = element('label');
      const checkbox = element('input');
      checkbox.type = 'checkbox'; checkbox.checked = item[key];
      checkbox.setAttribute('aria-label', `${title} for ${names[item.product]} ${item.id}`);
      checkbox.addEventListener('change', () => { item[key] = checkbox.checked; scheduleQuote(); });
      label.append(checkbox, document.createTextNode(title)); options.append(label);
    });
    row.append(options); container.append(row);
  });
  $('#item-count').textContent = items.length ? `${items.length} gift${items.length === 1 ? '' : 's'}, picked with care` : 'No gifts selected yet';
  document.querySelectorAll('.add').forEach(button => { button.disabled = items.length >= 12 || placing; });
}

function formData() {
  const form = new URLSearchParams({recipient: $('#recipient').value.trim(), message: $('#message').value,
    discount: $('#discount').value});
  items.forEach(item => form.append('item', `${item.product}:${Number(item.wrapped)}:${Number(item.ribbon)}`));
  return form;
}

async function request(path, form) {
  const response = await fetch(path, {method: 'POST', body: form});
  const result = await response.json();
  if (!response.ok) throw new Error(result.error || 'The request could not be completed.');
  return result;
}

function feedback(text, error = false) {
  $('#feedback').textContent = text;
  $('#feedback').classList.toggle('error', error);
}

function scheduleQuote() {
  revision++;
  currentQuote = null;
  $('#place').disabled = true;
  ['#subtotal', '#saving', '#total'].forEach(selector => { $(selector).textContent = '—'; });
  clearTimeout(quoteTimer);
  if (!items.length || !$('#recipient').value.trim()) {
    feedback('Add a gift and a recipient to see your total.');
    return;
  }
  feedback('Updating your box…');
  const version = revision;
  quoteTimer = setTimeout(() => updateQuote(version), 180);
}

async function updateQuote(version) {
  try {
    const quote = await request('/api/quote', formData());
    if (version !== revision) return;
    currentQuote = quote;
    $('#subtotal').textContent = money(quote.subtotal);
    $('#saving').textContent = '−' + money(quote.saving);
    $('#total').textContent = money(quote.total);
    $('#place').disabled = placing;
    feedback($('#discount').value === 'threshold' && quote.subtotal < 5000
      ? `${money(5000 - quote.subtotal)} more to unlock the big-box offer.` : 'Your box is ready when you are.');
  } catch (error) {
    if (version !== revision) return;
    feedback(error instanceof TypeError ? 'Cannot reach the Java server. Start the app and try again.' : error.message, true);
  }
}

function setEditing(enabled) {
  document.querySelectorAll('.builder input,.builder textarea,.builder button,#discount').forEach(control => { control.disabled = !enabled; });
  if (enabled) renderItems();
}

function renderReceipt() {
  $('#order').hidden = false;
  $('#order-id').textContent = receipt.id;
  $('#order-status').textContent = receipt.status;
  $('#order-summary').textContent = `For ${receipt.quote.recipient} · ${receipt.quote.items.length} gifts · ${money(receipt.quote.total)}`;
  [['#customer-events', receipt.customer], ['#packing-events', receipt.packing]].forEach(([selector, events]) => {
    const list = $(selector); list.replaceChildren();
    (events.length ? events : ['Waiting for the first status update.']).forEach(message => list.append(element('li', '', message)));
  });
  $('#advance').disabled = receipt.status === 'READY';
  $('#advance').textContent = receipt.status === 'CREATED' ? 'Start packing →' : receipt.status === 'PACKING' ? 'Mark ready →' : 'Ready to make their day ✓';
}

document.querySelectorAll('.add').forEach(button => button.addEventListener('click', () => {
  if (items.length >= 12) return;
  items.push({id: nextId++, product: button.dataset.product, wrapped: false, ribbon: false});
  renderItems(); scheduleQuote();
}));
$('#sample').addEventListener('click', () => {
  items = [{id: nextId++, product: 'book', wrapped: true, ribbon: true},
    {id: nextId++, product: 'candle', wrapped: false, ribbon: false}];
  $('#recipient').value = 'A friend'; $('#message').value = 'A small reminder that you matter.';
  $('#discount').value = 'student'; renderItems(); scheduleQuote();
});
['#recipient', '#message'].forEach(selector => $(selector).addEventListener('input', scheduleQuote));
$('#discount').addEventListener('change', scheduleQuote);
$('#place').addEventListener('click', async () => {
  if (!currentQuote || placing) return;
  placing = true; $('#place').disabled = true; setEditing(false);
  try {
    receipt = await request('/api/orders', formData());
    renderReceipt(); feedback('Your gift box has been created. Follow its progress below.');
    $('#order').scrollIntoView({behavior: 'smooth', block: 'start'});
  } catch (error) { setEditing(true); feedback(error.message, true); $('#place').disabled = false; }
  finally { placing = false; }
});
$('#advance').addEventListener('click', async () => {
  if (!receipt || receipt.status === 'READY') return;
  $('#advance').disabled = true;
  try {
    receipt = await request('/api/status', new URLSearchParams({id: receipt.id,
      status: receipt.status === 'CREATED' ? 'PACKING' : 'READY'}));
    renderReceipt();
  } catch (error) { feedback(error.message, true); $('#advance').disabled = false; }
});
$('#new-box').addEventListener('click', () => {
  receipt = null; $('#order').hidden = true; setEditing(true); scheduleQuote();
  $('.workspace').scrollIntoView({behavior: 'smooth', block: 'start'});
});
