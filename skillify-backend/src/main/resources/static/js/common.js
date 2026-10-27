const RM=matchMedia('(prefers-reduced-motion: reduce)').matches;
gsap.defaults({duration:RM?.01:.6,ease:'power3.out'});
const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)];
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const user=()=>JSON.parse(localStorage.getItem('skillify_user')||'null');
const login=r=>{localStorage.setItem('skillify_token',r.token);localStorage.setItem('skillify_user',JSON.stringify({name:r.name}))};
if(document.body.dataset.guard&&!user())location.href='login.html';
gsap.from('body',{opacity:0,duration:.4});

/* dark mode */
const root=document.documentElement;
root.dataset.theme=localStorage.getItem('skillify_theme')||(matchMedia('(prefers-color-scheme: dark)').matches?'dark':'light');
document.body.insertAdjacentHTML('beforeend','<button class="tbtn" id="theme" aria-label="Toggle dark mode">🌓</button>');
$('#theme').onclick=()=>{root.dataset.theme=root.dataset.theme=='dark'?'light':'dark';localStorage.setItem('skillify_theme',root.dataset.theme)};

/* toast + confetti helpers */
function toast(m){const t=document.createElement('div');t.className='toast';t.setAttribute('role','status');t.textContent=m;document.body.append(t);
 gsap.set(t,{xPercent:-50});gsap.from(t,{y:30,opacity:0});gsap.to(t,{opacity:0,y:20,delay:2.5,onComplete:()=>t.remove()})}
function confetti(){if(RM)return;const c=['#4f3cff','#ffe14d','#12a679','#e5484d'];
 for(let i=0;i<50;i++){const d=document.createElement('i');d.className='conf';d.style.background=c[i%4];document.body.append(d);
  gsap.to(d,{x:gsap.utils.random(-260,260),y:gsap.utils.random(-220,320),rotation:gsap.utils.random(-540,540),opacity:0,duration:gsap.utils.random(1.2,2),ease:'power2.out',onComplete:()=>d.remove()})}}

/* sidebar (landscape) / bottom tabs (portrait), same markup */
const shell=$('.shell');
if(shell){const p=location.pathname.split('/').pop();
 const L=[['home.html','🏠','Home'],['roadmap.html','🗺️','Roadmap'],['profile.html','👤','Profile']];
 shell.insertAdjacentHTML('afterbegin',`<nav class="nav"><div class="logo">Skillify</div>${L.map(l=>`<a href="${l[0]}" class="${p==l[0]?'on':''}"><b>${l[1]}</b><span>${l[2]}</span></a>`).join('')}<button class="out" id="out">Log out</button></nav>`);
 $('#out').onclick=()=>{localStorage.removeItem('skillify_token');localStorage.removeItem('skillify_user');location.href='index.html'};

 /* floating "Ask Skilly" chat on every app page */
 document.body.insertAdjacentHTML('beforeend','<button class="fab" id="fab" aria-label="Ask Skilly">💬</button><div class="card cp" id="cp" hidden><b>Ask Skilly</b><div class="msgs" id="cm" aria-live="polite"></div><form class="row" id="cf"><input id="ci" placeholder="Ask a doubt" aria-label="Your doubt" autocomplete="off"><button class="btn">Send</button></form></div>');
 const add=(t,me)=>{const d=document.createElement('div');d.className='b'+(me?' me':'');d.textContent=t;$('#cm').append(d);gsap.from(d,{y:12,opacity:0});$('#cm').scrollTop=1e5;return d};
 add("Hi, I'm Skilly. Stuck on something? Ask me.");
 $('#fab').onclick=()=>{const o=$('#cp').hidden;$('#cp').hidden=!o;if(o){gsap.fromTo('#cp',{y:30,opacity:0,scale:.95},{y:0,opacity:1,scale:1});$('#ci').focus()}};
 $('#cf').onsubmit=async e=>{e.preventDefault();const q=$('#ci').value.trim();if(!q)return;$('#ci').value='';add(q,1);const d=add('Thinking…');
  try{d.textContent=(await API.ask(q)).answer}catch(x){d.textContent=x.message}$('#cm').scrollTop=1e5};
 addEventListener('keydown',e=>{if(e.key==='Escape')$('#cp').hidden=true});
}

/* card tilt that follows the pointer */
$$('[data-tilt]').forEach(c=>{const rx=gsap.quickTo(c,'rotationX',{duration:.4}),ry=gsap.quickTo(c,'rotationY',{duration:.4});
 c.style.transformPerspective=600;
 c.onpointermove=e=>{const r=c.getBoundingClientRect();ry(((e.clientX-r.left)/r.width-.5)*12);rx(-((e.clientY-r.top)/r.height-.5)*12)};
 c.onpointerleave=()=>{rx(0);ry(0)}});

/* Skilly the mascot: eyes follow the pointer, click for a reaction */
const sk=$('#skilly');
if(sk){const pu=$$('.pu',sk),say=$('#say'),L=['Hi! Ready to learn?',"Let's find your weak spots.",'Quiz time?','Questions are my favourite.'];let n=0;
 addEventListener('pointermove',e=>{const r=sk.getBoundingClientRect(),dx=e.clientX-(r.left+r.width/2),dy=e.clientY-(r.top+r.height/2),d=Math.hypot(dx,dy)||1,m=Math.min(7,d/30);
  gsap.to(pu,{x:dx/d*m,y:dy/d*m,duration:.2,overwrite:true})});
 sk.onclick=()=>{say.textContent=L[n++%L.length];
  gsap.fromTo(sk,{scaleY:.85},{scaleY:1,duration:.9,ease:'elastic.out(1,.3)'});
  gsap.fromTo(say,{scale:.7,opacity:0},{scale:1,opacity:1})};
 sk.onkeydown=e=>e.key==='Enter'&&sk.click();
 if(!RM)gsap.to('#antenna',{rotation:8,transformOrigin:'50% 100%',yoyo:true,repeat:-1,duration:1.2,ease:'sine.inOut'});
}
