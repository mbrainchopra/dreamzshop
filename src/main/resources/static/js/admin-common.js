(function(){
  function ready(fn){ if(document.readyState==='loading') document.addEventListener('DOMContentLoaded',fn); else fn(); }
  ready(function(){
    var sidebar=document.getElementById('dsAdminSidebar');
    if(!sidebar) return;
    // Remove/hide legacy navigation so every admin page uses the same shell.
    document.querySelectorAll('.admin-sidebar,.sidebar,.dreamz-sidebar,.admin-global-nav,.admin-navbar,.topbar').forEach(function(el){
      if(!el.closest('.ds-sidebar')) el.style.setProperty('display','none','important');
    });
    // Collapse old sidebar columns and make their content full-width.
    document.querySelectorAll('.col-lg-2').forEach(function(col){
      if(col.querySelector('.admin-sidebar,.sidebar,.dreamz-sidebar')) col.style.setProperty('display','none','important');
    });
    document.querySelectorAll('.col-lg-10.main-content,.main-content').forEach(function(el){
      if(!el.closest('.ds-sidebar')) el.classList.add('ds-legacy-full');
    });
    // Active navigation.
    var path=window.location.pathname.replace(/\/$/,'');
    var links=sidebar.querySelectorAll('.ds-nav-link');
    links.forEach(function(link){
      var key=link.getAttribute('data-nav');
      var active=(key==='dashboard' && path==='/admin/dashboard') || (key!=='dashboard' && path.indexOf('/admin/'+key)===0);
      link.classList.toggle('active',active);
    });
    // Page title: prefer the visible H1, otherwise derive it from document.title.
    var title=document.getElementById('dsPageTitle'), h1=document.querySelector('h1:not(.ds-page-title)');
    if(title){
      var text=h1 && h1.textContent.trim();
      if(text) title.textContent=text;
      else {
        var raw=document.title.replace(/\s*[|–-]\s*Dreamz Shop.*$/i,'').replace(/^Dreamz Shop\s*[|–-]\s*/i,'').trim();
        if(raw) title.textContent=raw;
      }
    }
    // Mobile drawer.
    var toggle=document.querySelector('.ds-menu-toggle'), close=document.querySelector('.ds-sidebar-close'), backdrop=document.getElementById('dsSidebarBackdrop');
    function open(){sidebar.classList.add('open');backdrop.classList.add('show');if(toggle)toggle.setAttribute('aria-expanded','true');}
    function shut(){sidebar.classList.remove('open');backdrop.classList.remove('show');if(toggle)toggle.setAttribute('aria-expanded','false');}
    if(toggle)toggle.addEventListener('click',open); if(close)close.addEventListener('click',shut); if(backdrop)backdrop.addEventListener('click',shut);
    sidebar.querySelectorAll('a').forEach(function(a){a.addEventListener('click',function(){if(window.innerWidth<=991)shut();});});
  });
})();
