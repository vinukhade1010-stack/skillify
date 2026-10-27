/* All backend calls live here. Same address when served by Spring Boot (port 5000). */
const API=(()=>{
const BASE=location.port==='5000'?'/api':'http://localhost:5000/api';
const call=async(p,o={})=>{
 const t=p.startsWith('/auth')?null:localStorage.getItem('skillify_token');let r;
 try{r=await fetch(BASE+p,{...o,headers:{'Content-Type':'application/json',...(t&&{Authorization:'Bearer '+t})}})}
 catch(e){throw Error('Cannot reach the server. Is the backend running on port 5000?')}
 if(r.status===401&&t){localStorage.clear();location.href='login.html'}
 const d=await r.json().catch(()=>({}));
 if(!r.ok)throw Error(d.message||'Request failed');return d};
const post=(p,b)=>call(p,{method:'POST',body:JSON.stringify(b)});
return{
 login:(email,password)=>post('/auth/login',{email,password}),
 signup:b=>post('/auth/signup',b),
 topics:()=>call('/topics'),progress:()=>call('/progress'),
 quiz:topic=>post('/quiz/generate',{topic}),
 submit:(topic,score,wrong)=>post('/quiz/submit',{topic,score,wrong}),
 roadmaps:()=>call('/roadmap'),
 videos:q=>call('/videos?q='+encodeURIComponent(q)),
 ask:question=>post('/tutor/ask',{question}),
 profile:()=>call('/profile'),
 saveProfile:b=>call('/profile',{method:'PUT',body:JSON.stringify(b)})};
})();
